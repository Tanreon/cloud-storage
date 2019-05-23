package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Contracts.ProcessFailureException;
import com.geekbrains.cs.client.Header;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.common.OptionTypes.UploadOptionType;
import com.geekbrains.cs.common.Services.FileService;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFileRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.UPLOAD;
    private final OptionType OPTION_TYPE = UploadOptionType.FILE;

    private String fileName;
    private RandomAccessFile randomAccessFile;

    public UploadFileRequest(Channel channel, String fileName) {
        this.channel = channel;
        this.fileName = fileName;

        try {
            // Run data processing
            this.process();
            // Run protocol query processing
            this.sendDataByProtocol();
        } catch (ProcessFailureException ex) {
            LOGGER.log(Level.WARNING, "ProcessFailureException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Закачивание", ex.getMessage()));
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.WARNING, "FileNotFoundException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Закачивание", "Указанный файл не найден"));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Закачивание", ex.getMessage()));
        }
    }

    @Override
    protected void process() throws ProcessFailureException, FileNotFoundException {
        Path storage = Paths.get(Client.STORAGE_PATH, this.fileName);

        if (! storage.toFile().exists()) {
            throw new ProcessFailureException("Файл не найден в локальном каталоге");
        }

        this.randomAccessFile = new RandomAccessFile(storage.toFile(), "rw");
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() throws IOException {
        long filePartsCount = (long) Math.ceil((double) this.randomAccessFile.length() / Common.BUFFER_LENGTH);

        for (int filePart = 0; filePart < filePartsCount; filePart++) {
            LOGGER.log(Level.INFO, "Sending file part, {0} of file: {1}", new Object[]{ filePart, this.fileName });

            this.outByteBuf.retain();

            { // write meta
                this.writeRequest(new Request(ACTION_TYPE, OPTION_TYPE, false));
            }

            { // write head
                this.writeHeader(new Header(HeaderType.AUTH, Client.getAuth().getKey()));
            }

            { // write file name
                this.writeStringByShort(this.fileName);
            }

            { // write full file size in Common.BUFFER_LENGTH bytes block
                this.outByteBuf.writeLong(filePartsCount);
            }

            { // write bytes part
                this.outByteBuf.writeInt(filePart);
            }

            { // write data
                long availableBytes = FileService.availableBytes(filePart, this.randomAccessFile);

                byte[] buffer = new byte[(int) availableBytes];

                this.randomAccessFile.read(buffer);

                this.outByteBuf.writeLong(availableBytes);
                this.outByteBuf.writeBytes(buffer);
            }

            { // write end
                this.writeEndBytes();
            }

            this.channel.writeAndFlush(this.outByteBuf, this.channel.newProgressivePromise()).syncUninterruptibly();

            this.outByteBuf.clear();
        }

        this.randomAccessFile.close();
    }
}

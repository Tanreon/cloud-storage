package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Contracts.ProcessFailureException;
import com.geekbrains.cs.client.Controllers.MainController;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.UploadOptionType;
import com.geekbrains.cs.common.Services.FileService;
import io.netty.channel.Channel;
import io.netty.channel.DefaultFileRegion;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFileRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.UPLOAD;
    private final OptionType OPTION_TYPE = UploadOptionType.FILE;

    private String fileName;
    private File file;

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
        }
    }

    @Override
    protected void process() throws ProcessFailureException {
        Path storage = Paths.get(Client.STORAGE_PATH, this.fileName);

        if (! storage.toFile().exists()) {
            throw new ProcessFailureException("Файл не найден в локальном каталоге");
        }

        this.file = storage.toFile();
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        long filePartsCount = (long) Math.ceil((double) this.file.length() / Common.BUFFER_LENGTH);

        for (int filePart = 0; filePart < filePartsCount; filePart++) {
            LOGGER.log(Level.INFO, "Sending file part, {0} of file: {1}", new Object[]{ filePart, this.fileName });

            { // write meta
                channel.write(new Request(ACTION_TYPE, OPTION_TYPE, false));
            }

            { // write head

            }

            { // write file name
                this.writeStringByShort(this.fileName);
            }

            { // write full file size in Common.BUFFER_LENGTH bytes block
                channel.write(filePartsCount);
            }

            { // write bytes part
                channel.write(filePart);
            }

            { // write data
                long availableBytes = FileService.availableBytes(filePart, this.file);

                channel.write(availableBytes);
                channel.write(new DefaultFileRegion(this.file, Common.BUFFER_LENGTH * filePart, availableBytes));
            }

            { // write end
                this.writeEndBytes();
            }

            this.channel.flush();
        }
    }
}

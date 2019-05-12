package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.DownloadOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String STORAGE_PATH = "server_storage";

    private final ActionType ACTION_TYPE = ActionType.DOWNLOAD;
    private final OptionType OPTION_TYPE = DownloadOptionType.FILE;

    ////////////////////
    private String login = "test";
    private String fileName;
    private RandomAccessFile randomAccessFile;
    ////////////////////

    public DownloadFileAction(ChannelHandlerContext ctx, ByteBuf message) throws Exception {
        this.ctx = ctx;
        this.byteBuf = message;

        // Run protocol request processing
        if (!this.receiveDataByProtocol()) {
            return;
        }

        // Run request processing
        if (!this.run()) {
            return;
        }

        // Run protocol answer processing
        if (!this.sendDataByProtocol()) {
            return;
        }
    }

    @Override
    protected boolean receiveDataByProtocol() {
        if (! byteBuf.isReadable()) {
            rejectEmpty(ACTION_TYPE, OPTION_TYPE);
            return false;
        }

        try {
            int fileNameLength = this.byteBuf.readInt();
            byte[] fileNameBytes = new byte[fileNameLength];
            this.byteBuf.readBytes(fileNameBytes);
            this.fileName = new String(fileNameBytes);

            LOGGER.log(Level.INFO, "{0} -> File name receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> File name receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "FILE_NAME_NOT_CORRECT"));

            return false;
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            return false;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }

        return true;
    }

    @Override
    protected boolean run() throws Exception {
        Path storage = Paths.get(STORAGE_PATH, this.login, this.fileName);

        if (! storage.toFile().exists()) {
            LOGGER.log(Level.INFO, "{0} -> File not found: {1}", new Object[] { this.ctx.channel().id(), this.fileName });
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 404, "FILE_NOT_FOUND"));

            return false;
        }

        this.randomAccessFile = new RandomAccessFile(storage.toFile(), "r");

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() throws IOException {
        LOGGER.log(Level.INFO, "{0} -> Client success start download file part: {1}", new Object[] { this.ctx.channel().id(), this.fileName });

        long filePartsCount = (long) Math.ceil((double) this.randomAccessFile.length() / 4096);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        for (int filePart = 1; filePart <= filePartsCount; filePart++) { // TODO оптимизировать
            LOGGER.log(Level.INFO, "{0} -> Sending file part, {1} of file: {2}", new Object[]{this.ctx.channel().id(), filePart, this.fileName});

            {
                outputStream.write(this.resp(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK", false))); // FIXME возможно нет ничего плохого в том что делается два write на сокете. Дублирование кода
            }

            { // write file name
                byte[] fileNameBytes = this.fileName.getBytes();
                outputStream.writeInt(fileNameBytes.length); // {1}
                outputStream.write(fileNameBytes); // {2}
            }

            { // write full file size in 4096 bytes block
                outputStream.writeLong(filePartsCount); // {3}
            }

            { // write bytes part
                outputStream.writeInt(filePart); // {4}

                long availableBytes = (filePart * 4096) < this.randomAccessFile.length() ? 4096 : 4096 - ((filePart * 4096) - this.randomAccessFile.length());

                byte[] fileBytes = new byte[(int) availableBytes];
                int bytesRead = this.randomAccessFile.read(fileBytes);
                outputStream.writeInt(bytesRead); // {5}
                outputStream.write(fileBytes); // {6}
            }

            {
                outputStream.write(new byte[]{(byte) 0, (byte) -1});
            }

            this.ctx.writeAndFlush(Unpooled.wrappedBuffer(byteOutputStream.toByteArray()));

            byteOutputStream.reset();
        }

        this.randomAccessFile.close();

        return true;
    }
}

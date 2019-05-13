package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.DownloadOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

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

    private final long BUFFER_LENGTH = 60 * 1024;

    ////////////////////
    private String login = "test";
    private String fileName;
    private RandomAccessFile randomAccessFile;
    ////////////////////

    public DownloadFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

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
        if (! this.byteBuf.isReadable()) {
            rejectEmpty(ACTION_TYPE, OPTION_TYPE);
            return false;
        }

        try {
            this.fileName = this.readStringByInt();

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

        long filePartsCount = (long) Math.ceil((double) this.randomAccessFile.length() / BUFFER_LENGTH);

        for (int filePart = 1; filePart <= filePartsCount; filePart++) {
            LOGGER.log(Level.INFO, "{0} -> Sending file part, {1} of file: {2}", new Object[]{this.ctx.channel().id(), filePart, this.fileName});

            {
                ctx.write(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
            }

            ByteBuf byteBuf = Unpooled.directBuffer();

            { // write file name
                byte[] fileNameBytes = this.fileName.getBytes();
                byteBuf.writeInt(fileNameBytes.length); // {1}
                byteBuf.writeBytes(fileNameBytes); // {2}
            }

            { // write full file size in 4096 bytes block
                byteBuf.writeLong(filePartsCount); // {3}
            }

            { // write bytes part
                byteBuf.writeInt(filePart); // {4}

                long availableBytes;

                if (BUFFER_LENGTH > this.randomAccessFile.length()) {
                    availableBytes = this.randomAccessFile.length();
                } else {
                    if ((filePart * BUFFER_LENGTH) > this.randomAccessFile.length()) {
                        availableBytes = this.randomAccessFile.length() - ((filePart - 1) * BUFFER_LENGTH);
                    } else {
                        availableBytes = BUFFER_LENGTH;
                    }
                }

                byte[] fileBytes = new byte[(int) availableBytes];
                int bytesRead = this.randomAccessFile.read(fileBytes);
                byteBuf.writeInt(bytesRead); // {5}
                byteBuf.writeBytes(fileBytes); // {6}
            }

            {
                byteBuf.writeBytes(Server.getEndBytes());
            }

            this.ctx.writeAndFlush(byteBuf);
        }

        this.randomAccessFile.close();

        return true;
    }
}

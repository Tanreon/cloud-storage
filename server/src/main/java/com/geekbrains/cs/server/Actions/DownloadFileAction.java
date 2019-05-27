package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.*;
import com.geekbrains.cs.common.OptionTypes.DownloadOptionType;
import com.geekbrains.cs.common.Services.FileService;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.Auth;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.DOWNLOAD;
    private final OptionType OPTION_TYPE = DownloadOptionType.FILE;

    private Auth auth;

    private String fileName;
    private RandomAccessFile randomAccessFile;

    public DownloadFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf, Auth auth) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;
        this.auth = auth;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", this.ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (EmptyRequestException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyRequestException", this.ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", this.ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.INFO, "{0} -> ProcessException: {1}", new Object[]{ctx.channel().id(), ex.getMessage()});
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "{0} -> IOException: {1}", new Object[]{ctx.channel().id(), ex.getMessage()});
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
        }
    }

    /**
     * protocol: [fileNameLength][fileNameBytes]
     * */
    @Override
    protected void receiveDataByProtocol() throws IncorrectEndException, EmptyRequestException {
        if (! this.inByteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // read file name
            this.fileName = this.readStringByShort();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException, FileNotFoundException {
        Path storage = Paths.get(Server.STORAGE_PATH, this.auth.getLogin(), this.fileName);

        if (! storage.toFile().exists()) {
            throw new ProcessException(404, "FILE_NOT_FOUND");
        }

        this.randomAccessFile = new RandomAccessFile(storage.toFile(), "rw");
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() throws IOException {
        LOGGER.log(Level.INFO, "{0} -> Success start sending file part: {1}", new Object[] { this.ctx.channel().id(), this.fileName });

        long filePartsCount = (long) Math.ceil((double) this.randomAccessFile.length() / Common.BUFFER_LENGTH);

        for (int filePart = 0; filePart < filePartsCount; filePart++) {
            LOGGER.log(Level.INFO, "{0} --> Sending file part, {1} of file: {2}", new Object[]{ this.ctx.channel().id(), filePart, this.fileName });

            this.outByteBuf.retain();

            { // write meta
                this.writeAction(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
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

            this.ctx.writeAndFlush(this.outByteBuf, this.ctx.newProgressivePromise()).syncUninterruptibly();

            this.outByteBuf.clear();
        }
    }
}

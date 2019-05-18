package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.EmptyRequestException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.DownloadOptionType;
import com.geekbrains.cs.common.Services.FileService;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.common.Contracts.ProcessException;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.DOWNLOAD;
    private final OptionType OPTION_TYPE = DownloadOptionType.FILE;

    ////////////////////
    private String login = "test";
    ////////////////////

    private String fileName;
    private File file;

    public DownloadFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (EmptyRequestException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyRequestException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.INFO, "{0} -> ProcessException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        }
    }

    /**
     * protocol: [fileNameLength][fileNameBytes]
     * */
    @Override
    protected void receiveDataByProtocol() throws IncorrectEndException, EmptyRequestException {
        if (! this.byteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // read file name
            this.fileName = this.readStringByShort();
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        Path storage = Paths.get(Server.STORAGE_PATH, this.login, this.fileName);

        if (! storage.toFile().exists()) {
            throw new ProcessException(404, "FILE_NOT_FOUND");
        }

        this.file = storage.toFile();
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Success start sending file part: {1}", new Object[] { this.ctx.channel().id(), this.fileName });

        long filePartsCount = (long) Math.ceil((double) this.file.length() / Common.BUFFER_LENGTH);

        for (int filePart = 0; filePart < filePartsCount; filePart++) {
            LOGGER.log(Level.INFO, "{0} --> Sending file part, {1} of file: {2}", new Object[]{ this.ctx.channel().id(), filePart, this.fileName });

            { // write head
                ctx.write(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
            }

            { // write file name
                this.writeStringByShort(this.fileName);
            }

            { // write full file size in Common.BUFFER_LENGTH bytes block
                ctx.write(filePartsCount);
            }

            { // write bytes part
                ctx.write(filePart);
            }

            { // write data
                long availableBytes = FileService.availableBytes(filePart, this.file);

                ctx.write(availableBytes);
                ctx.write(new DefaultFileRegion(this.file, Common.BUFFER_LENGTH * filePart, availableBytes));
            }

            { // write end
                this.writeEndBytes();
            }

            this.ctx.flush();
        }
    }
}

package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.EmptyRequestException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.UploadOptionType;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.UPLOAD;
    private final OptionType OPTION_TYPE = UploadOptionType.FILE;

    ////////////////////
    private String login = "test";
    ////////////////////

    private String fileName;
    private long filePartsCount;
    private int fileCurrentPart;
    private byte[] fileDataBytes;

    public UploadFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (EmptyRequestException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyRequestException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "{0} -> IOException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
        }
    }

    /**
     * protocol: [fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes]
     * */
    @Override
    protected void receiveDataByProtocol() throws IncorrectEndException, EmptyRequestException {
        if (! this.inByteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // get filename
            this.fileName = this.readStringByShort();
        }

        { // get full file size in Common.BUFFER_LENGTH bytes block
            this.filePartsCount = this.inByteBuf.readLong();
        }

        { // get file part
            this.fileCurrentPart = this.inByteBuf.readInt();
        }

        { // get data
            this.fileDataBytes = this.readBytesByLong();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws IOException {
        File file = Paths.get(Server.STORAGE_PATH, this.login, this.fileName).toFile();

        if (this.fileCurrentPart == 0 && file.exists()) {
            file.delete();
        }

        while (true) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                randomAccessFile.seek(this.fileCurrentPart * Common.BUFFER_LENGTH);
                randomAccessFile.write(this.fileDataBytes);

                break;
            } catch (FileNotFoundException ignored) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][fileNamePartsCount][fileNameCurrentPart][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Success received file part, {1} of file: {2}", new Object[]{ this.ctx.channel().id(), this.fileCurrentPart, this.fileName });

        { // write head
            this.writeAction(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
        }

        { // write file name
            this.writeStringByShort(this.fileName);
        }

        { // write file parts count
            this.outByteBuf.writeLong(this.filePartsCount);
        }

        { // write file current part
            this.outByteBuf.writeInt(this.fileCurrentPart);
        }

        { // write end
            this.writeEndBytes();
        }

        this.ctx.writeAndFlush(this.outByteBuf);
    }
}

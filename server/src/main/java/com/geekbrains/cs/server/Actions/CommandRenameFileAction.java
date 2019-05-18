package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.EmptyRequestException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.CommandOptionType;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.common.Contracts.ProcessException;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandRenameFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.RENAME_FILE;

    ////////////////////
    private String login = "test";
    ////////////////////

    private String currentFileName;
    private String newFileName;

    public CommandRenameFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
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
     * protocol: [currentFileNameLength][currentFileNameBytes][newFileNameLength][newFileNameBytes]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyRequestException, IncorrectEndException {
        if (! this.byteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // read current file name
            this.currentFileName = this.readStringByShort();
        }

        { // read new file name
            this.newFileName = this.readStringByShort();
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        Path oldFilePath = Paths.get(Server.STORAGE_PATH, this.login, this.currentFileName);

        if (! oldFilePath.toFile().exists()) {
            throw new ProcessException(404, "CURRENT_FILE_NOT_FOUND");
        }

        Path newFilePath = Paths.get(Server.STORAGE_PATH, this.login, this.newFileName);

        if (newFilePath.toFile().exists()) {
            throw new ProcessException(403, "NEW_FILE_EXISTS");
        }

        oldFilePath.toFile().renameTo(newFilePath.toFile());
    }

    /**
     * protocol: [RESPONSE][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Success renamed file: {1}", new Object[]{this.ctx.channel().id(), this.newFileName});

        { // write head
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK"));
        }
    }
}

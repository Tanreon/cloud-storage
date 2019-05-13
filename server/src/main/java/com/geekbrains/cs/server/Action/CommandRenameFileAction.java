package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandRenameFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String STORAGE_PATH = "server_storage";

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.RENAME_FILE;

    ////////////////////
    private String login = "test";
    private String oldFileName;
    private String newFileName;
    ////////////////////

    public CommandRenameFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
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
        if (!this.byteBuf.isReadable()) {
            rejectEmpty(ACTION_TYPE, OPTION_TYPE);

            return false;
        }

        try {
            this.oldFileName = this.readStringByInt();

            LOGGER.log(Level.INFO, "{0} -> Old file name receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Old file name receiving exception: {1}", new Object[]{this.ctx.channel().id(), ex.getMessage()});
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "OLD_FILE_NAME_NOT_CORRECT"));

            return false;
        }

        try {
            this.newFileName = this.readStringByInt();

            LOGGER.log(Level.INFO, "{0} -> New file name receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> New file name receiving exception: {1}", new Object[]{this.ctx.channel().id(), ex.getMessage()});
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "NEW_FILE_NAME_NOT_CORRECT"));

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
    protected boolean run() {
        Path oldFilePath = Paths.get(STORAGE_PATH, this.login, this.oldFileName);

        if (!oldFilePath.toFile().exists()) {
            LOGGER.log(Level.INFO, "{0} -> File not found: {1}", new Object[]{this.ctx.channel().id(), this.oldFileName});
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 404, "OLD_FILE_NOT_FOUND"));

            return false;
        }

        Path newFilePath = Paths.get(STORAGE_PATH, this.login, this.newFileName);

        if (newFilePath.toFile().exists()) {
            LOGGER.log(Level.INFO, "{0} -> File not found: {1}", new Object[]{this.ctx.channel().id(), this.oldFileName});
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 403, "NEW_FILE_EXISTS"));

            return false;
        }

        oldFilePath.toFile().renameTo(newFilePath.toFile());

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Client success renamed file: {1}", new Object[]{this.ctx.channel().id(), this.newFileName});
        ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK"));

        return true;
    }
}

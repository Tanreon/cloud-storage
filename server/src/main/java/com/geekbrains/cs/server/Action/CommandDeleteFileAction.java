package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandDeleteFileAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String STORAGE_PATH = "server_storage";

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.DELETE_FILE;

    ////////////////////
    private String login = "test";
    private String fileName;
    ////////////////////

    public CommandDeleteFileAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
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
            this.fileName = this.readStringByInt();

            LOGGER.log(Level.INFO, "{0} -> File name receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> File name receiving exception: {1}", new Object[]{this.ctx.channel().id(), ex.getMessage()});
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
    protected boolean run() {
        Path filePath = Paths.get(STORAGE_PATH, this.login, this.fileName);

        if (!filePath.toFile().exists()) {
            LOGGER.log(Level.INFO, "{0} -> File not found: {1}", new Object[]{this.ctx.channel().id(), this.fileName});
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 404, "FILE_NOT_FOUND"));

            return false;
        }

        filePath.toFile().delete();

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Client success deleted file: {1}", new Object[]{this.ctx.channel().id(), this.fileName});
        ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK"));

        return true;
    }
}

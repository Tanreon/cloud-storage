package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.server.Response;
import io.netty.channel.ChannelHandlerContext;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private ChannelHandlerContext ctx;
    private ActionType actionType;
    private OptionType optionType;
    private int status;
    private String message;

    public ErrorAction(ChannelHandlerContext ctx, ActionType actionType, OptionType optionType, int status) {
        this.ctx = ctx;
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;

        LOGGER.log(Level.INFO, "{0} -> Err request", ctx.channel().id());

        // Run request processing
        this.run();
    }

    public ErrorAction(ChannelHandlerContext ctx, ActionType actionType, OptionType optionType, int status, String message) {
        this.ctx = ctx;
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;

        LOGGER.log(Level.INFO, "{0} -> Err request", ctx.channel().id());

        // Run request processing
        this.run();
    }

    private void run() {
        Response response = new Response(this.actionType, this.optionType, this.status);

        if (this.message.length() > 0) {
            response.setMessage(this.message);
        }

        ctx.writeAndFlush(response);
    }
}

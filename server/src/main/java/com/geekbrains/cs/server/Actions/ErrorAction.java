package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.server.ActionResponse;
import io.netty.channel.ChannelHandlerContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private ActionType actionType;
    private OptionType optionType;
    private int status;
    private String message;

    public ErrorAction(ChannelHandlerContext ctx, ActionType actionType, OptionType optionType) {
        this(ctx, actionType, optionType, 404, "UNKNOWN_REQUEST");
    }

    public ErrorAction(ChannelHandlerContext ctx, ActionType actionType, OptionType optionType, int status, String message) {
        this.ctx = ctx;
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;

        // Run protocol answer processing
        this.sendDataByProtocol();
    }

    @Override
    protected void receiveDataByProtocol() {
        throw new NotImplementedException();
    }

    @Override
    protected void process() {
        throw new NotImplementedException();
    }

    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());

        { // write head
            this.writeActionAndFlush(new ActionResponse(this.actionType, this.optionType, this.status, this.message));
        }
    }
}

package com.geekbrains.cs.server.Middlewares;

import com.geekbrains.cs.server.Actions.AbstractAction;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.server.Server;
import io.netty.channel.ChannelHandlerContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorMiddleware extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private ChannelHandlerContext ctx;
    private HeaderType actionType;
    private String message;

    public ErrorMiddleware(ChannelHandlerContext ctx, HeaderType headerType) {
        this(ctx, headerType, "UNKNOWN_HEADER");
    }

    public ErrorMiddleware(ChannelHandlerContext ctx, HeaderType headerType, String message) {
        this.ctx = ctx;
        this.actionType = headerType;
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
        LOGGER.log(Level.WARNING, "{0} -> Err header", ctx.channel().id());

        { // write head
            ctx.writeAndFlush(new MiddlewareResponse(this.actionType, this.message));
        }
    }
}

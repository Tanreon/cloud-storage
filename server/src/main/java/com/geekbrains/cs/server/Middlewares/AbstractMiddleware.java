package com.geekbrains.cs.server.Middlewares;

import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.common.Responses.BaseAbstractResponse;
import com.geekbrains.cs.server.Handlers.InMiddlewareHandler;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.server.Server;
import io.netty.channel.ChannelFuture;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

public abstract class AbstractMiddleware extends BaseAbstractResponse {
    protected static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    protected LinkedHashMap<HeaderType, String> headersMap;
    protected boolean result = false;

    public boolean canCallNext() {
        return this.result;
    }

    protected void next() {
        this.result = true;
    }

    abstract public void init();

    protected void writeMiddleware(MiddlewareResponse response) {
        this.outByteBuf.writeByte(response.getHeaderType().getValue());

        if (response.hasMessage()) {
            byte[] messageBytes = response.getMessage().getBytes();

            this.outByteBuf.writeInt(messageBytes.length);
            this.outByteBuf.writeBytes(messageBytes);
        } else {
            this.outByteBuf.writeInt(0);
        }

        this.writeEndBytes();
    }

    protected ChannelFuture writeMiddlewareAndFlush(MiddlewareResponse response) {
        this.writeMiddleware(response);
        return this.ctx.writeAndFlush(this.outByteBuf);
    }
}

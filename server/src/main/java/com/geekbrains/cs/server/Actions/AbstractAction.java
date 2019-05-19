package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.Responses.BaseAbstractResponse;
import com.geekbrains.cs.server.AbstractResponse;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.MiddlewareResponse;
import io.netty.channel.ChannelFuture;

public abstract class AbstractAction extends BaseAbstractResponse {
    protected abstract void sendDataByProtocol() throws Exception;

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
        this.ctx.writeAndFlush(this.outByteBuf).syncUninterruptibly();
    }

    protected void writeAction(AbstractResponse response) {
        this.outByteBuf.writeByte(response.getActionType().getValue());
        this.outByteBuf.writeByte(response.getOptionType().getValue());
        this.outByteBuf.writeShort(response.getStatus());

        if (response.hasMessage()) {
            byte[] messageBytes = response.getMessage().getBytes();

            this.outByteBuf.writeInt(messageBytes.length);
            this.outByteBuf.writeBytes(messageBytes);
        } else {
            this.outByteBuf.writeInt(0);
        }

        if (response.isLast()) {
            this.writeEndBytes();
        }
    }

    protected ChannelFuture writeMiddlewareAndFlush(MiddlewareResponse response) {
        this.writeMiddleware(response);
        return this.ctx.writeAndFlush(this.outByteBuf);
    }

    protected ChannelFuture writeActionAndFlush(ActionResponse response) {
        this.writeAction(response);
        return this.ctx.writeAndFlush(this.outByteBuf);
    }

    protected ChannelFuture writeActionAndFlush(AbstractResponse response) {
        this.writeAction(response);
        return this.ctx.writeAndFlush(this.outByteBuf);
    }
}

package com.geekbrains.cs.server.Middlewares;

import com.geekbrains.cs.common.Responses.BaseAbstractResponse;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.server.AbstractResponse;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;

import java.util.LinkedHashMap;
import java.util.logging.Logger;

public abstract class AbstractMiddleware extends BaseAbstractResponse {
    protected static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    protected LinkedHashMap<HeaderType, String> headersMap = new LinkedHashMap<>();

    protected LinkedHashMap<HeaderType, String> readHeaders() {
        byte headersCount = this.inByteBuf.readByte();

        LinkedHashMap<HeaderType, String> headersMap = new LinkedHashMap<>();

        for (int i = 0; i < headersCount; i++) {
            byte headerTypeByte = this.inByteBuf.readByte();
            String header = this.readStringByShort();

            headersMap.put(HeaderType.fromByte(headerTypeByte), header);
        }

        return headersMap;
    }

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

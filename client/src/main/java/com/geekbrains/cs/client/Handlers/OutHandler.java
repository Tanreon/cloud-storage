package com.geekbrains.cs.client.Handlers;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.Common;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof Request) {
            ctx.write(writeByProtocol((Request) msg), promise);
        } else if (msg instanceof Short){
            ctx.write(Unpooled.copyShort((short) msg), promise);
        } else if (msg instanceof Integer){
            ctx.write(Unpooled.copyInt((int) msg), promise);
        } else if (msg instanceof Long){
            ctx.write(Unpooled.copyLong((long) msg), promise);
        } else if (msg instanceof byte[]){
            ctx.write(Unpooled.copiedBuffer((byte[]) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private ByteBuf writeByProtocol(Request request) {
        ByteBuf byteBuf = Unpooled.directBuffer();

        byteBuf.writeByte(request.getActionType().getValue());
        byteBuf.writeByte(request.getOptionType().getValue());

        if (request.isLast()) {
            byteBuf.writeBytes(Common.END_BYTES);
        }

        return Unpooled.wrappedBuffer(byteBuf);
    }
}

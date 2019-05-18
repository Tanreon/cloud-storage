package com.geekbrains.cs.server.Handlers;

import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class OutHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof ActionResponse) {
            ctx.write(writeByProtocol((ActionResponse) msg), promise);
        } else if (msg instanceof MiddlewareResponse) {
            ctx.write(writeByProtocol((MiddlewareResponse) msg), promise);
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

    private ByteBuf writeByProtocol(MiddlewareResponse middlewareResponse) {
        ByteBuf byteBuf = Unpooled.directBuffer();

        byteBuf.writeByte(middlewareResponse.getHeaderType().getValue());

        if (middlewareResponse.hasMessage()) {
            byte[] messageBytes = middlewareResponse.getMessage().getBytes();

            byteBuf.writeInt(messageBytes.length);
            byteBuf.writeBytes(messageBytes);
        } else {
            byteBuf.writeInt(0);
        }

        byteBuf.writeBytes(Common.END_BYTES);

        return Unpooled.wrappedBuffer(byteBuf);
    }

    private ByteBuf writeByProtocol(ActionResponse actionResponse) {
        ByteBuf byteBuf = Unpooled.directBuffer();

        byteBuf.writeByte(actionResponse.getActionType().getValue());
        byteBuf.writeByte(actionResponse.getOptionType().getValue());
        byteBuf.writeShort((short) actionResponse.getStatus());

        if (actionResponse.hasMessage()) {
            byte[] messageBytes = actionResponse.getMessage().getBytes();

            byteBuf.writeInt(messageBytes.length);
            byteBuf.writeBytes(messageBytes);
        } else {
            byteBuf.writeInt(0);
        }

        if (actionResponse.isLast()) {
            byteBuf.writeBytes(Common.END_BYTES);
        }

        return Unpooled.wrappedBuffer(byteBuf);
    }
}

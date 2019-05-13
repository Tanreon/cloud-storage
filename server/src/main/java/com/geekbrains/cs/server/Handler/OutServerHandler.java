package com.geekbrains.cs.server.Handler;

import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) { // TODO переделать, не нравится дублировние кодав action и тут
        if (msg instanceof Response) {
            ctx.write(writeByProtocol((Response) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private ByteBuf writeByProtocol(Response response) {
        ByteBuf byteBuf = Unpooled.directBuffer();

        byteBuf.writeByte(response.getActionType().getValue());
        byteBuf.writeByte(response.getOptionType().getValue());
        byteBuf.writeShort((short) response.getStatus());

        if (response.hasMessage()) {
            byte[] messageBytes = response.getMessage().getBytes();

            byteBuf.writeInt(messageBytes.length);
            byteBuf.writeBytes(messageBytes);
        } else {
            byteBuf.writeInt(0);
        }

        if (response.last()) {
            byteBuf.writeBytes(Server.getEndBytes());
        }

        return Unpooled.wrappedBuffer(byteBuf);
    }
}

package com.geekbrains.cloud_storage.Handler;

import com.geekbrains.cloud_storage.Server;
import com.geekbrains.cloud_storage.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        LOGGER.log(Level.INFO, "----> write out");

        if (msg instanceof Response) {
            ctx.writeAndFlush(generateDataByProtocol((Response) msg), promise);
        } else {
            ctx.writeAndFlush(msg, promise);
        }
    }

    private ByteBuf generateDataByProtocol(Response response) {
        ByteBuf[] byteBufs;

        if (response.hasMessage()) {
            byteBufs = new ByteBuf[5];

        } else {
            byteBufs = new ByteBuf[3];
        }

        byteBufs[0] = Unpooled.buffer(1).writeByte(response.getActionType().getValue());
        byteBufs[1] = Unpooled.buffer(1).writeByte(response.getOptionType().getValue());
        byteBufs[2] = Unpooled.copyShort(response.getStatus());

        if (response.hasMessage()) {
            byte[] messageBytes = response.getMessage().getBytes();

            byteBufs[3] = Unpooled.copyInt(messageBytes.length);
            byteBufs[4] = Unpooled.wrappedBuffer(messageBytes);
        }

        return Unpooled.wrappedBuffer(byteBufs);
    }
}

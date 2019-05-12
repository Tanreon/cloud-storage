package com.geekbrains.cloud_storage.Handler;

import com.geekbrains.cloud_storage.Client;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;

public class OutClientHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

//    private final byte[] endBytes = new byte[] { (byte) 0, (byte) -1 };

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof ByteArrayOutputStream) {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(((ByteArrayOutputStream) msg).toByteArray()));
        } else {
            ctx.write(msg, promise);
        }
    }
}

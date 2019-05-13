package com.geekbrains.cs.client.Handler;

import com.geekbrains.cs.client.Client;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Logger;

public class OutClientHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        ctx.write(msg, promise);
    }
}

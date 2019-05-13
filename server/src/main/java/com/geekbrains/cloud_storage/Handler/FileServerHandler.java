package com.geekbrains.cloud_storage.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.io.RandomAccessFile;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;

        RandomAccessFile raf = null;
        long length = -1;
        try {
            raf = new RandomAccessFile("storage/test/" + "winbox.exe", "r");
            length = raf.length();
        } catch (Exception e) {
            ctx.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
            return;
        } finally {
            if (length < 0 && raf != null) {
                raf.close();
            }
        }

        ctx.write("OK: " + raf.length() + '\n');
        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, length));
        ctx.writeAndFlush("\n");

        ctx.writeAndFlush("EOF: done" + '\n');
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
    }
}

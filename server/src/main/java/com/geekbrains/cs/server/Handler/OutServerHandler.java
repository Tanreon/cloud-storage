package com.geekbrains.cs.server.Handler;

import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

public class OutServerHandler extends ChannelOutboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final byte[] endBytes = new byte[] { (byte) 0, (byte) -1 };

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws IOException { // TODO переделать, не нравится дублировние кодав action и тут
        if (msg instanceof Response) {
            ctx.write(generateDataByProtocol((Response) msg), promise);
        } else {
            ctx.write(msg, promise);
        }
    }

    private ByteBuf generateDataByProtocol(Response response) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        outputStream.write(response.getActionType().getValue());
        outputStream.write(response.getOptionType().getValue());
        outputStream.writeShort((short) response.getStatus());

        if (response.hasMessage()) {
            byte[] messageBytes = response.getMessage().getBytes();

            outputStream.writeInt(messageBytes.length);
            outputStream.write(messageBytes);
        } else {
            outputStream.writeInt(0);
        }

        if (response.last()) {
            outputStream.write(this.endBytes);
        }

        return Unpooled.wrappedBuffer(byteOutputStream.toByteArray());
    }
}

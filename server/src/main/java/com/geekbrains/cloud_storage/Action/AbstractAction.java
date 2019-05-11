package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Contract.OptionType;
import com.geekbrains.cloud_storage.Response;
import com.geekbrains.cloud_storage.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractAction {
    protected static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    protected ChannelHandlerContext ctx;
    protected ByteBuf message;

    protected abstract boolean receiveDataByProtocol() throws Exception;
    protected abstract boolean run() throws Exception;
    protected abstract boolean sendDataByProtocol() throws Exception;

    protected void rejectEmpty(ActionType actionType, OptionType optionType) {
        LOGGER.log(Level.WARNING, "{0} -> Protocol receiving err", ctx.channel().id());
        ctx.writeAndFlush(new Response(actionType, optionType, 400, "BAD_REQUEST"));
        ctx.close();
    }

    protected byte[] resp(Response response) throws IOException {
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
            outputStream.write(new byte[]{(byte) 0, (byte) -1});
        }

        return byteOutputStream.toByteArray();
    }
}

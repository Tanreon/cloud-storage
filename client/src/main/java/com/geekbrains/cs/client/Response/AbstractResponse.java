package com.geekbrains.cs.client.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractResponse {
    protected ChannelHandlerContext ctx;
    protected ByteBuf byteBuf;

    protected short status;
    protected String message;

    protected abstract void run() throws Exception;
    protected abstract void receiveDataByProtocol() throws Exception;

    protected void readMeta() {
        { // read status
            this.status = this.byteBuf.readShort();
        }

        { // read message
            int messageLength = this.byteBuf.readInt();

            if (messageLength != 0) {
                byte[] messageBytes = new byte[messageLength];
                this.byteBuf.readBytes(messageBytes);
                this.message = new String(messageBytes);
            }
        }
    }

    protected String readString() {
        int stringLength = this.byteBuf.readInt();
        byte[] stringBytes = new byte[stringLength];

        this.byteBuf.readBytes(stringBytes);

        return new String(stringBytes);
    }

    protected byte[] readBytes() {
        int bytesLength = this.byteBuf.readInt();
        byte[] bytes = new byte[bytesLength];
        this.byteBuf.readBytes(bytes);

        return bytes;
    }
}

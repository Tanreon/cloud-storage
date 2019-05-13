package com.geekbrains.cs.common.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class BaseAbstractResponse {
    protected ChannelHandlerContext ctx;
    protected ByteBuf byteBuf;

    protected String readStringByInt() {
        return this.readString(this.byteBuf.readInt());
    }

    protected String readStringByShort() {
        return this.readString(this.byteBuf.readShort());
    }

    protected byte[] readBytes() {
        int bytesLength = this.byteBuf.readInt();
        byte[] bytes = new byte[bytesLength];

        this.byteBuf.readBytes(bytes);

        return bytes;
    }

    private String readString(int length) {
        byte[] stringBytes = new byte[length];

        this.byteBuf.readBytes(stringBytes);

        return new String(stringBytes);
    }
}
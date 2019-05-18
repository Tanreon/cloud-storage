package com.geekbrains.cs.common.Responses;

import com.geekbrains.cs.common.BaseAbstractNetworkInteraction;
import com.geekbrains.cs.common.Common;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public abstract class BaseAbstractResponse extends BaseAbstractNetworkInteraction {
    protected ChannelHandlerContext ctx;
    protected ByteBuf byteBuf;

    protected abstract void receiveDataByProtocol() throws Exception;

    protected String readStringByInt() {
        return this.readString(this.byteBuf.readInt());
    }

    protected String readStringByShort() {
        return this.readString(this.byteBuf.readShort());
    }

    protected String readStringByLength(int length) {
        return this.readString(length);
    }

    protected byte[] readBytesByLong() {
        return this.readBytes((int) this.byteBuf.readLong());
    }

    protected byte[] readBytesByInt() {
        return this.readBytes(this.byteBuf.readInt());
    }

    protected byte[] readBytes(int length) {
        byte[] bytes = new byte[length];

        this.byteBuf.readBytes(bytes);

        return bytes;
    }

    private String readString(int length) {
        byte[] stringBytes = new byte[length];
        this.byteBuf.readBytes(stringBytes);

        return new String(stringBytes);
    }

    protected void writeStringByShort(String string) {
        byte[] stringBytes = string.getBytes();
        short stringBytesLength = (short) stringBytes.length;

        ctx.write(stringBytesLength);
        ctx.write(stringBytes);
    }

    protected void writeStringByInt(String string) {
        byte[] stringBytes = string.getBytes();
        int stringBytesLength = stringBytes.length;

        ctx.write(stringBytesLength);
        ctx.write(stringBytes);
    }

    protected void writeEndBytes() {
        ctx.write(Common.END_BYTES);
    }
}
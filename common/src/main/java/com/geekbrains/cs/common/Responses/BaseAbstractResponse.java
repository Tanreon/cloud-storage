package com.geekbrains.cs.common.Responses;

import com.geekbrains.cs.common.BaseAbstractNetworkInteraction;
import com.geekbrains.cs.common.Common;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public abstract class BaseAbstractResponse extends BaseAbstractNetworkInteraction {
    protected ChannelHandlerContext ctx;
    protected ByteBuf inByteBuf;
    protected ByteBuf outByteBuf = Unpooled.directBuffer();

    protected abstract void receiveDataByProtocol() throws Exception;

    protected String readStringByInt() {
        return this.readString(this.inByteBuf.readInt());
    }

    public String readStringByShort() {
        return this.readString(this.inByteBuf.readShort());
    }

    protected byte[] readBytesByLong() {
        return this.readBytes((int) this.inByteBuf.readLong());
    }

    protected byte[] readBytesByInt() {
        return this.readBytes(this.inByteBuf.readInt());
    }

    protected byte[] readBytes(int length) {
        byte[] bytes = new byte[length];

        this.inByteBuf.readBytes(bytes);

        return bytes;
    }

    protected String readString(int length) {
        byte[] stringBytes = new byte[length];
        this.inByteBuf.readBytes(stringBytes);

        return new String(stringBytes);
    }

    protected void writeStringByShort(String string) {
        byte[] stringBytes = string.getBytes();
        short stringBytesLength = (short) stringBytes.length;

        this.outByteBuf.writeShort(stringBytesLength);
        this.outByteBuf.writeBytes(stringBytes);
    }

    protected void writeStringByInt(String string) {
        byte[] stringBytes = string.getBytes();
        int stringBytesLength = stringBytes.length;

        this.outByteBuf.writeInt(stringBytesLength);
        this.outByteBuf.writeBytes(stringBytes);
    }

    protected void writeEndBytes() {
        this.outByteBuf.writeBytes(Common.END_BYTES);
    }
}
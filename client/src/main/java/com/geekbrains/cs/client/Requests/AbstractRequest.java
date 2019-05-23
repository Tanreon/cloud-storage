package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Header;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.BaseAbstractNetworkInteraction;
import com.geekbrains.cs.common.Common;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

abstract public class AbstractRequest extends BaseAbstractNetworkInteraction {
    protected Channel channel;
    protected ByteBuf outByteBuf = Unpooled.directBuffer();

    protected abstract void sendDataByProtocol() throws Exception;

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

    protected void writeRequest(Request request) {
        this.outByteBuf.writeByte(request.getActionType().getValue());
        this.outByteBuf.writeByte(request.getOptionType().getValue());

        if (request.isLast()) {
            this.writeEndBytes();
        }
    }

    protected void writeHeaders(Header[] headers) {
        this.outByteBuf.writeByte(headers.length);

        for (Header header : headers) {
            this.outByteBuf.writeByte(header.getHeaderType().getValue());
            this.writeStringByShort(header.getValue());
        }
    }

    protected void writeHeader(Header header) {
        this.writeHeaders(new Header[] { header });
    }
}
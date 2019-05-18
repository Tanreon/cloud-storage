package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.common.BaseAbstractNetworkInteraction;
import com.geekbrains.cs.common.Common;
import io.netty.channel.Channel;

abstract public class AbstractRequest extends BaseAbstractNetworkInteraction {
    protected Channel channel;

    protected abstract void sendDataByProtocol() throws Exception;

    protected void writeStringByShort(String string) {
        byte[] stringBytes = string.getBytes();
        short stringBytesLength = (short) stringBytes.length;

        channel.write(stringBytesLength);
        channel.write(stringBytes);
    }

    protected void writeStringByInt(String string) {
        byte[] stringBytes = string.getBytes();
        int stringBytesLength = stringBytes.length;

        channel.write(stringBytesLength);
        channel.write(stringBytes);
    }

    protected void writeEndBytes() {
        channel.write(Common.END_BYTES);
    }
}
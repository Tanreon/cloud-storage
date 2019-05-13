package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.common.Response.BaseAbstractResponse;

public abstract class AbstractResponse extends BaseAbstractResponse {
    protected short status;
    protected String message;

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
}

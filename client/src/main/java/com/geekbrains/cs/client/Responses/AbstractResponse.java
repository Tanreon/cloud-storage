package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.common.Responses.BaseAbstractResponse;

public abstract class AbstractResponse extends BaseAbstractResponse {
    protected short status;
    protected String message;

    protected void readMeta() {
        { // read status
            this.status = this.inByteBuf.readShort();
        }

        { // read message
            int messageLength = this.inByteBuf.readInt();

            if (messageLength != 0) {
                this.message = this.readString(messageLength);
            }
        }
    }
}

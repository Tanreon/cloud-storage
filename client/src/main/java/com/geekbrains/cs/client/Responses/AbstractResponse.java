package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.common.Responses.BaseAbstractResponse;

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
                this.message = this.readStringByLength(messageLength);
            }
        }
    }
}

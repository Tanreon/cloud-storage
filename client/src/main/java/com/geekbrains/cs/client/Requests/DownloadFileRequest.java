package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.DownloadOptionType;
import io.netty.channel.Channel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Logger;

public class DownloadFileRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.DOWNLOAD;
    private final OptionType OPTION_TYPE = DownloadOptionType.FILE;

    private String fileName;

    public DownloadFileRequest(Channel channel, String fileName) {
        this.channel = channel;
        this.fileName = fileName;

        // Run protocol query processing
        this.sendDataByProtocol();
    }

    @Override
    protected void process() {
        throw new NotImplementedException();
    }

    /**
     * protocol: [REQUEST][HEADERS][fileNameLength][fileNameBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        { // write meta
            this.writeRequest(new Request(ACTION_TYPE, OPTION_TYPE, false));
        }

        { // write head

        }

        { // write file name
            this.writeStringByShort(this.fileName);
        }

        { // write end bytes
            this.writeEndBytes();
        }

        this.channel.writeAndFlush(this.outByteBuf).syncUninterruptibly();
    }
}

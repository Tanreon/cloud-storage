package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Header;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.common.OptionTypes.CommandOptionType;
import io.netty.channel.Channel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Logger;

public class CommandFileListRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.FILE_LIST;

    public CommandFileListRequest(Channel channel) {
        this.channel = channel;

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
            this.writeHeader(new Header(HeaderType.AUTH, Client.getAuth().getKey()));
        }

        { // write end bytes
            this.writeEndBytes();
        }

        this.channel.writeAndFlush(this.outByteBuf);
    }
}

package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.CommandOptionType;
import io.netty.channel.Channel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Logger;

public class CommandRenameFileRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.RENAME_FILE;

    private String currentFileName;
    private String newFileName;

    public CommandRenameFileRequest(Channel channel, String currentFileName, String newFileName) {
        this.channel = channel;
        this.currentFileName = currentFileName;
        this.newFileName = newFileName;

        // Run protocol query processing
        this.sendDataByProtocol();
    }

    @Override
    protected void process() {
        throw new NotImplementedException();
    }

    /**
     * protocol: [REQUEST][HEADERS][currentFileNameLength][currentFileNameBytes][newFileNameLength][newFileNameBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        { // write meta
            this.writeRequest(new Request(ACTION_TYPE, OPTION_TYPE, false));
        }

        { // write head

        }

        { // write current file name
            this.writeStringByShort(this.currentFileName);
        }

        { // write new file name
            this.writeStringByShort(this.newFileName);
        }

        { // write end bytes
            this.writeEndBytes();
        }

        this.channel.writeAndFlush(this.outByteBuf).syncUninterruptibly();
    }
}

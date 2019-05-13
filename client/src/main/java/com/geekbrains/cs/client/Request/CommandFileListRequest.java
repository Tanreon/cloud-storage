package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandFileListRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.FILE_LIST;

    public CommandFileListRequest() {
        // Run protocol processing
        this.sendDataByProtocol();
    }

    private void sendDataByProtocol() {
        ByteBuf byteBuf = Unpooled.directBuffer();

        {
            byteBuf.writeBytes(new byte[]{ACTION_TYPE.getValue(), OPTION_TYPE.getValue()});
            LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
        }

        {
            byteBuf.writeBytes(Client.getEndBytes());
            LOGGER.log(Level.INFO, "End write: {0}", ACTION_TYPE);
        }

        Client.getNetworkChannel().writeAndFlush(byteBuf);
    }
}
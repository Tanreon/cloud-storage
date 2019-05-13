package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandRenameFileRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.RENAME_FILE;

    private String oldFileName;
    private String newFileName;

    public CommandRenameFileRequest(String oldFileName, String newFileName) {
        this.oldFileName = oldFileName;
        this.newFileName = newFileName;

        // Run protocol processing
        this.sendDataByProtocol();
    }

    protected void sendDataByProtocol() {
        ByteBuf byteBuf = Unpooled.directBuffer();

        {
            byteBuf.writeBytes(new byte[]{ACTION_TYPE.getValue(), OPTION_TYPE.getValue()});
            LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
        }

        {
            byte[] oldFileNameBytes = this.oldFileName.getBytes();
            byteBuf.writeInt(oldFileNameBytes.length);
            byteBuf.writeBytes(oldFileNameBytes);

            byte[] newNameBytes = this.newFileName.getBytes();
            byteBuf.writeInt(newNameBytes.length);
            byteBuf.writeBytes(newNameBytes);

            LOGGER.log(Level.INFO, "Data write: {0}", ACTION_TYPE);
        }

        {
            byteBuf.writeBytes(Client.getEndBytes());
            LOGGER.log(Level.INFO, "End write: {0}", ACTION_TYPE);
        }

        Client.getNetworkChannel().writeAndFlush(byteBuf);
    }
}

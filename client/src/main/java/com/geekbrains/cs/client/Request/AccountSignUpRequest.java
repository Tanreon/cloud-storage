package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.AccountOptionType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_UP;

    private String login;
    private String email;
    private String password;

    public AccountSignUpRequest(String login, String email, String password) {
        this.login = login;
        this.email = email;
        this.password = password;

        // Run request processing
        if (! this.run()) {
            return;
        }

        // Run protocol answer processing
        if (! this.sendDataByProtocol()) {
            return;
        }
    }

    @Override
    protected boolean run() {
        return true;
    }

    @Override
    protected boolean sendDataByProtocol() {
        ByteBuf byteBuf = Unpooled.directBuffer();

        {
            byteBuf.writeBytes(new byte[]{ACTION_TYPE.getValue(), OPTION_TYPE.getValue()});
            LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
        }

        {
            byte[] loginBytes = this.login.getBytes();
            byteBuf.writeShort((short) loginBytes.length);
            byteBuf.writeBytes(loginBytes);

            byte[] emailBytes = this.email.getBytes();
            byteBuf.writeShort((short) emailBytes.length);
            byteBuf.writeBytes(emailBytes);

            byte[] passwordBytes = this.password.getBytes();
            byteBuf.writeShort((short) passwordBytes.length);
            byteBuf.writeBytes(passwordBytes);

            LOGGER.log(Level.INFO, "Data write: {0}", ACTION_TYPE);
        }

        {
            byteBuf.writeBytes(Common.END_BYTES);
            LOGGER.log(Level.INFO, "End write: {0}", ACTION_TYPE);
        }

        Client.getNetworkChannel().writeAndFlush(byteBuf);

        return true;
    }
}

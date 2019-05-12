package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.AccountOptionType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpRequest {
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

        // Run protocol processing
        this.sendDataByProtocol();
    }

    private void sendDataByProtocol() {
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

            {
                outputStream.write(new byte[] { ACTION_TYPE.getValue(), OPTION_TYPE.getValue() });
                LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
            }

            {
                byte[] loginBytes = this.login.getBytes();
                outputStream.writeShort((short) loginBytes.length);
                outputStream.write(loginBytes);

                byte[] emailBytes = this.email.getBytes();
                outputStream.writeShort((short) emailBytes.length);
                outputStream.write(emailBytes);

                byte[] passwordBytes = this.password.getBytes();
                outputStream.writeShort((short) passwordBytes.length);
                outputStream.write(passwordBytes);

                LOGGER.log(Level.INFO, "Data write: {0}", ACTION_TYPE);
            }

            {
                outputStream.write(Client.getEndBytes());
                LOGGER.log(Level.INFO, "End write: {0}", ACTION_TYPE);
            }

            Client.getNetworkChannel().writeAndFlush(byteOutputStream);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Send exception: {0}", ex.getMessage());
        }
    }
}

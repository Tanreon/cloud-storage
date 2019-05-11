package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.Contract.OptionType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
                outputStream.write(Client.getNetwork().getEndBytes());

                LOGGER.log(Level.INFO, "Meta write: {0}", ACTION_TYPE);
            }

            {
                outputStream.writeShort((short) this.login.length());
                outputStream.write(this.login.getBytes());

                outputStream.writeShort((short) this.email.length());
                outputStream.write(this.email.getBytes());

                outputStream.writeShort((short) this.password.length());
                outputStream.write(this.password.getBytes());

                outputStream.write(Client.getNetwork().getEndBytes());

                LOGGER.log(Level.INFO, "Data write: {0}", ACTION_TYPE);
            }

            Client.getNetwork().writeAndFlush(byteOutputStream);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Send exception: {0}", ex.getMessage());
        }
    }
}
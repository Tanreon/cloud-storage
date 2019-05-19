package com.geekbrains.cs.client.Requests;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Request;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.AccountOptionType;
import io.netty.channel.Channel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.logging.Logger;

public class AccountSignUpRequest extends AbstractRequest {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_UP;

    private String login;
    private String email;
    private String password;

    public AccountSignUpRequest(Channel channel, String login, String email, String password) {
        this.channel = channel;
        this.login = login;
        this.email = email;
        this.password = password;

        // Run protocol query processing
        this.sendDataByProtocol();
    }

    @Override
    protected void process() {
        throw new NotImplementedException();
    }

    /**
     * protocol: [REQUEST][HEADERS][loginLength][loginBytes][emailLength][emailBytes][passwordLength][passwordBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        { // write meta
            this.writeRequest(new Request(ACTION_TYPE, OPTION_TYPE, false));
        }

        { // write head

        }

        { // write login
            this.writeStringByShort(this.login);
        }

        { // write email
            this.writeStringByShort(this.email);
        }

        { // write password
            this.writeStringByShort(this.password);
        }

        { // write end bytes
            this.writeEndBytes();
        }

        this.channel.writeAndFlush(this.outByteBuf).syncUninterruptibly();
    }
}

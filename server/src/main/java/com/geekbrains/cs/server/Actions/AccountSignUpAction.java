package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.EmptyRequestException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.AccountOptionType;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.Contracts.InvalidRequestInputException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Services.AccountSignService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_UP;

    private String login = null;
    private String email = null;
    private String password = null;

    public AccountSignUpAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (EmptyRequestException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyRequestException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", ctx.channel().id());
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.INFO, "{0} -> ProcessException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        } catch (InvalidRequestInputException ex) {
            LOGGER.log(Level.INFO, "{0} -> InvalidRequestInputException: {1}", new Object[]{ ctx.channel().id(), ex.getMessage() });
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        }
    }

    /**
     * protocol: [loginLength][loginBytes][emailLength][emailBytes][passwordLength][passwordBytes]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyRequestException, IncorrectEndException {
        if (! this.inByteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // read login
            this.login = this.readStringByShort();
        }

        { // read email
            this.email = this.readStringByShort();
        }

        { // read password
            this.password = this.readStringByShort();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws InvalidRequestInputException, ProcessException {
        if (this.login.length() < 2) {
            throw new InvalidRequestInputException(400, "LOGIN_LENGTH_SMALL");
        }

        // TODO добавить проверку email по Regex
        if (this.email.length() < 5) {
            throw new InvalidRequestInputException(400, "EMAIL_LENGTH_SMALL");
        }

        if (this.password.length() < 4) {
            throw new InvalidRequestInputException(400, "PASSWORD_LENGTH_SMALL");
        }

        if (AccountSignService.isLoginExists(this.login)) {
            throw new ProcessException(403, "LOGIN_ALREADY_EXISTS");
        }

        if (AccountSignService.isEmailExists(this.email)) {
            throw new ProcessException(403, "EMAIL_ALREADY_EXISTS");
        }

        AccountSignService.create(this.login, this.email, this.password);
    }

    /**
     * protocol: [RESPONSE][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Success signed up: {1}", new Object[] { this.ctx.channel().id(), this.login });

        { // write head
            this.writeActionAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK"));
        }
    }
}

package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.EmptyRequestException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.AccountOptionType;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.Contracts.EmptyResultException;
import com.geekbrains.cs.server.Contracts.InvalidRequestInputException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import com.geekbrains.cs.server.Contracts.ProcessFailureException;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Services.AccountSignService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_IN;

    private String login = null;
    private String password = null;
    private String key = null;

    public AccountSignInAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (EmptyRequestException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyRequestException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.INFO, "{0} -> ProcessException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        } catch (InvalidRequestInputException ex) {
            LOGGER.log(Level.INFO, "{0} -> InvalidRequestInputException: {1}", new Object[]{ ctx.channel().id(), ex.getMessage() });
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, ex.getStatus(), ex.getMessage()));
        } catch (ProcessFailureException ex) {
            LOGGER.log(Level.WARNING, "{0} -> ProcessFailureException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            ctx.writeAndFlush(ex.getResponse());
        }
    }

    /**
    * protocol: [loginLength][loginBytes][passwordLength][passwordBytes]
    * */
    @Override
    protected void receiveDataByProtocol() throws EmptyRequestException, IncorrectEndException {
        if (! this.byteBuf.isReadable()) {
            throw new EmptyRequestException();
        }

        { // read login
            this.login = this.readStringByShort();
        }

        { // read password
            this.password = this.readStringByShort();
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessFailureException, InvalidRequestInputException, ProcessException {
        if (this.login.length() < 2) {
            throw new InvalidRequestInputException(400, "LOGIN_LENGTH_SMALL");
        }

        if (this.password.length() < 4) {
            throw new InvalidRequestInputException(400, "PASSWORD_LENGTH_SMALL");
        }

        if (! AccountSignService.isLoginExists(this.login)) {
            throw new ProcessException(404, "LOGIN_NOT_FOUND");
        }

        if (! AccountSignService.isCredentialsCorrect(this.login, this.password)) {
            throw new ProcessException(403, "CREDENTIALS_NOT_CORRECT");
        }

        try {
            // FIXME если пользователь уже проходил процесс аутентификации с определенного IP или например ID приложения
            // FIXME то новый ключ не генерировать, если уже существует старый то выдать его
            this.key = AccountSignService.generateKeyByCredentials(this.login);
        } catch (EmptyResultException ex) {
            throw new ProcessFailureException(ex, new ActionResponse(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
        }
    }

    /**
     * protocol: [RESPONSE][keyLength][keyBytes][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Success signed in: {1}", new Object[]{ this.ctx.channel().id(), this.login });

        { // write head
            ctx.write(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
        }

        { // write key
            this.writeStringByShort(this.key);
        }

        { // write end
            this.writeEndBytes();
        }

        this.ctx.flush();
    }
}

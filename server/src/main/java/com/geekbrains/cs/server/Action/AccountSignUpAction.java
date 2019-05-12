package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.AccountOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Service.AccountSignService;
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

    public AccountSignUpAction(ChannelHandlerContext ctx, ByteBuf message) throws Exception {
        this.ctx = ctx;
        this.byteBuf = message;

        // Run protocol request processing
        if (! this.receiveDataByProtocol()) {
            return;
        }

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
    protected boolean receiveDataByProtocol() {
        if (! byteBuf.isReadable()) {
            rejectEmpty(ACTION_TYPE, OPTION_TYPE);
            return false;
        }

        try {
            short loginLength = this.byteBuf.readShort();
            byte[] loginBytes = new byte[loginLength];
            this.byteBuf.readBytes(loginBytes);
            this.login = new String(loginBytes);

            LOGGER.log(Level.INFO, "{0} -> Login receiving success: {1}", new Object[] { this.ctx.channel().id(), this.login });
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Login receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "LOGIN_NOT_CORRECT"));

            return false;
        }

        try {
            short emailLength = this.byteBuf.readShort();
            byte[] emailBytes = new byte[emailLength];
            this.byteBuf.readBytes(emailBytes);
            this.email = new String(emailBytes);

            LOGGER.log(Level.INFO, "{0} -> Email receiving success: {1}", new Object[] { this.ctx.channel().id(), this.email });
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Email receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "EMAIL_NOT_CORRECT"));

            return false;
        }

        try {
            short passwordLength = this.byteBuf.readShort();
            byte[] passwordBytes = new byte[passwordLength];
            this.byteBuf.readBytes(passwordBytes);
            this.password = new String(passwordBytes);

            LOGGER.log(Level.INFO, "{0} -> Password receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Password receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "PASSWORD_NOT_CORRECT"));

            return false;
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            return false;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }

        return true;
    }

    @Override
    protected boolean run() {
        if (this.login.length() < 2) {
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "LOGIN_LENGTH_SMALL"));

            return false;
        }

        // TODO добавить проверку email по Regex
        if (this.email.length() < 5) {
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "EMAIL_LENGTH_SMALL"));

            return false;
        }

        if (this.password.length() < 4) {
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "PASSWORD_LENGTH_SMALL"));

            return false;
        }

        AccountSignService accountSignService = new AccountSignService();

        if (accountSignService.isLoginExists(this.login)) {
            LOGGER.log(Level.INFO, "{0} -> Login already exists: {1}", new Object[] { this.ctx.channel().id(), this.login });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 403, "LOGIN_ALREADY_EXISTS"));

            return false;
        }

        if (accountSignService.isEmailExists(this.email)) {
            LOGGER.log(Level.INFO, "{0} -> Email already exists: {1}", new Object[] { this.ctx.channel().id(), this.login });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 403, "EMAIL_ALREADY_EXISTS"));

            return false;
        }

        accountSignService.create(this.login, this.email, this.password);

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> Client success signed up: {1}", new Object[] { this.ctx.channel().id(), this.login });
        this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK"));

        return true;
    }
}

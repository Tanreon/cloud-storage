package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.AccountOptionType;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Service.AccountSignService;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.server.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    
    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_IN;

    private String login = null;
    private String password = null;

    private String key = null;

    public AccountSignInAction(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

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
    protected boolean run() throws Exception {
        if (this.login.length() < 2) {
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "LOGIN_LENGTH_SMALL"));

            return false;
        }

        if (this.password.length() < 4) {
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "PASSWORD_LENGTH_SMALL"));

            return false;
        }

        AccountSignService accountSignService = new AccountSignService();

        if (! accountSignService.isLoginExists(this.login)) {
            LOGGER.log(Level.INFO, "{0} -> Login not found: {1}", new Object[] { this.ctx.channel().id(), this.login });
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 404, "LOGIN_NOT_FOUND"));

            return false;
        }

        if (! accountSignService.isCredentialsCorrect(this.login, this.password)) {
            LOGGER.log(Level.INFO, "{0} -> Credentials not correct: {1}", new Object[] { this.ctx.channel().id(), this.login });
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 403, "CREDENTIALS_NOT_CORRECT"));

            return false;
        }

        this.key = accountSignService.generateKeyByCredentials(this.login); // FIXME я понимаю что это не лучший метод аутентификации на сервере, но пока так

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() throws IOException {
        LOGGER.log(Level.INFO, "{0} -> Client success signed in: {1}", new Object[] { this.ctx.channel().id(), this.login });

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        {
            outputStream.write(this.resp(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK", false))); // FIXME возможно нет ничего плохого в том что делается два write на сокете. Дублирование кода
        }

        {
            LOGGER.log(Level.INFO, "{0} -> Client key: {1}", new Object[]{this.ctx.channel().id(), this.key});
            byte[] keyBytes = this.key.getBytes();
            int keyBytesLength = keyBytes.length;

            outputStream.writeInt(keyBytesLength);
            outputStream.write(keyBytes);
        }

        {
            outputStream.write(new byte[] { (byte) 0, (byte) -1 });
        }

        this.ctx.writeAndFlush(Unpooled.wrappedBuffer(byteOutputStream.toByteArray()));

        return true;
    }
}

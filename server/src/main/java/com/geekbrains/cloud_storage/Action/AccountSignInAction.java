package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Contract.OptionType;
import com.geekbrains.cloud_storage.Server;
import com.geekbrains.cloud_storage.Service.AccountSignService;
import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Response;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    
    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_IN;

    private String login = null;
    private String password = null;

    private String key = null;

    public AccountSignInAction(ChannelHandlerContext ctx, ByteBuf message) throws Exception {
        this.ctx = ctx;
        this.message = message;

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
        if (! message.isReadable()) {
            rejectEmpty(ACTION_TYPE, OPTION_TYPE);
            return false;
        }

        try {
            short loginLength = this.message.readShort();
            byte[] loginBytes = new byte[loginLength];
            this.message.readBytes(loginBytes);
            this.login = new String(loginBytes);

            LOGGER.log(Level.INFO, "{0} -> Login receiving success: {1}", new Object[] { this.ctx.channel().id(), this.login });
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Login receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "LOGIN_NOT_CORRECT"));

            return false;
        }

        try {
            short passwordLength = this.message.readShort();
            byte[] passwordBytes = new byte[passwordLength];
            this.message.readBytes(passwordBytes);
            this.password = new String(passwordBytes);

            LOGGER.log(Level.INFO, "{0} -> Password receiving success", this.ctx.channel().id());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "{0} -> Password receiving exception: {1}", new Object[] { this.ctx.channel().id(), ex.getMessage() });
            this.ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "PASSWORD_NOT_CORRECT"));

            return false;
        }

//        ByteBuf dataEndBytes = Unpooled.buffer(2);
//        this.message.readBytes(dataEndBytes);
//
//        if (dataEndBytes.readByte() == (byte)0 && dataEndBytes.readByte() == (byte)-1) {  // TODO перенести в общий класс
//            LOGGER.log(Level.INFO, "data end correct");
//        } else {
//            LOGGER.log(Level.INFO, "data end NOT correct");
//
//            return false;
//        }

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

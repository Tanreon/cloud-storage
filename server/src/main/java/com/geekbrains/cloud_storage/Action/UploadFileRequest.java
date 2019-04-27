package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Contract.OptionType;
import com.geekbrains.cloud_storage.Response;
import com.geekbrains.cloud_storage.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFileRequest {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.UPLOAD;
    private final OptionType OPTION_TYPE = UploadOptionType.FILE;

    private ChannelHandlerContext ctx;
    private ByteBuf message;

    public UploadFileRequest(ChannelHandlerContext ctx, ByteBuf message) {
        this.ctx = ctx;
        this.message = message;

        // Run protocol processing
        this.receiveDataByProtocol();
        // Run request processing
        this.run();
    }

    private void run() {
//        AccountAuthService authService = new AccountAuthService();
//
//        if (! authService.isLoginExists(this.login)) {
//            LOGGER.log(Level.INFO, "{0} -> Login not found: {1}", new Object[] { ctx.channel().id(), this.login });
//
//            ctx.writeAndFlush(Converter.answer(ActionType.ACCOUNT, 404, AccountOptionType.AUTH, "LOGIN_NOT_FOUND"));
//            ctx.close();
//            return;
//        }
//
//        if (! authService.isCredentialsCorrect(this.login, this.password)) {
//            LOGGER.log(Level.INFO, "{0} -> Credentials not correct: {1}", new Object[] { ctx.channel().id(), this.login });
//
//            ctx.writeAndFlush(Converter.answer(ActionType.ACCOUNT, 403, AccountOptionType.AUTH, "CREDENTIALS_NOT_CORRECT"));
//            ctx.close();
//            return;
//        }
//
//        LOGGER.log(Level.INFO, "{0} -> Client success signed in: {1}", new Object[] { ctx.channel().id(), this.login });
//        ctx.writeAndFlush(Converter.answer(ActionType.ACCOUNT, 200, AccountOptionType.AUTH, "OK"));
    }

    private void receiveDataByProtocol() {
        if (! message.isReadable()) {
            LOGGER.log(Level.WARNING, "{0} -> Protocol receiving err", ctx.channel().id());
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
            ctx.close();
            return;
        }



//        try {
//            short loginLength = message.readShort();
//            byte[] loginBytes = new byte[loginLength];
//            message.readBytes(loginBytes);
//            this.login = new String(loginBytes);
//
//            LOGGER.log(Level.INFO, "{0} -> Login receiving success: {1}", new Object[] { ctx.channel().id(), this.login });
//        } catch (Exception ex) {
//            LOGGER.log(Level.WARNING, "{0} -> Login receiving exception: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
//
//            ctx.writeAndFlush(Converter.answer(ActionType.ACCOUNT, 400, AccountOptionType.AUTH, "LOGIN_NOT_CORRECT"));
//            ctx.close();
//            return;
//        }
//
//        try {
//            short passwordLength = message.readShort();
//            byte[] passwordBytes = new byte[passwordLength];
//            message.readBytes(passwordBytes);
//            this.password = new String(passwordBytes);
//
//            LOGGER.log(Level.INFO, "{0} -> Password receiving success", ctx.channel().id());
//        } catch (Exception ex) {
//            LOGGER.log(Level.WARNING, "{0} -> Password receiving exception: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
//
//            ctx.writeAndFlush(Converter.answer(ActionType.ACCOUNT, 400, AccountOptionType.AUTH, "PASSWORD_NOT_CORRECT"));
//            ctx.close();
//            return;
//        }
    }
}

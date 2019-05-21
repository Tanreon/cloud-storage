package com.geekbrains.cs.server.Handlers;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.OptionTypes.*;
import com.geekbrains.cs.server.Actions.*;
import com.geekbrains.cs.server.Contracts.MiddlewareEvent;
import com.geekbrains.cs.server.Events.AuthMiddlewareEvent;
import com.geekbrains.cs.server.Middlewares.AuthMiddleware;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private LinkedHashMap<String, MiddlewareEvent> middlewareEventMap = new LinkedHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        LOGGER.log(Level.INFO, "{0} -> Client connected", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.log(Level.INFO, "{0} -> Client disconnected", ctx.channel().id());

        if (! (cause instanceof IOException) && ! (cause instanceof IndexOutOfBoundsException)) {
            cause.printStackTrace();
        }

        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof AuthMiddlewareEvent) {
            this.middlewareEventMap.put("authMiddleware", (AuthMiddlewareEvent) evt);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte actionTypeByte = byteBuf.readByte();
        byte optionTypeByte = byteBuf.readByte();
        LOGGER.log(Level.INFO, "{0} -> action: {1}, option: {2}", new Object[] { ctx.channel().id(), actionTypeByte, optionTypeByte });

        ActionType actionType = ActionType.fromByte(actionTypeByte);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);

                switch (accountOptionType) {
                    case SIGN_IN:
                        new AccountSignInAction(ctx, byteBuf);
                        break;
                    case SIGN_UP:
                        new AccountSignUpAction(ctx, byteBuf);
                        break;
                    case CHANGE_PASS:
                    case CHANGE_LOGIN:
                    case DELETE_ACCOUNT:
                    default:
                        new ErrorAction(ctx, actionType, accountOptionType);
                }
                break;
            case DOWNLOAD:
                // middleware process
                new AuthMiddleware(ctx, byteBuf);

                if (! this.middlewareEventMap.containsKey("authMiddleware")) {
                    break;
                }

                if (! ((AuthMiddlewareEvent) this.middlewareEventMap.get("authMiddleware")).isSignedIn()) {
                    break;
                }

                // action process
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);

                switch (downloadOptionType) {
                    case FILE:
                        new DownloadFileAction(ctx, byteBuf, this.middlewareEventMap);
                        break;
                    default:
                        new ErrorAction(ctx, actionType, downloadOptionType);
                }
                break;
            case UPLOAD:
                // middleware process
                new AuthMiddleware(ctx, byteBuf);

                if (! this.middlewareEventMap.containsKey("authMiddleware")) {
                    break;
                }

                if (! ((AuthMiddlewareEvent) this.middlewareEventMap.get("authMiddleware")).isSignedIn()) {
                    break;
                }

                // action process
                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);

                switch (uploadOptionType) {
                    case FILE:
                        new UploadFileAction(ctx, byteBuf, this.middlewareEventMap);
                        break;
                    default:
                        new ErrorAction(ctx, actionType, uploadOptionType);
                }
                break;
            case COMMAND:
                // middleware process
                new AuthMiddleware(ctx, byteBuf);

                if (! this.middlewareEventMap.containsKey("authMiddleware")) {
                    break;
                }

                if (! ((AuthMiddlewareEvent) this.middlewareEventMap.get("authMiddleware")).isSignedIn()) {
                    break;
                }

                // action process
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);

                switch (commandOptionType) {
                    case FILE_LIST:
                        new CommandFileListAction(ctx, byteBuf, this.middlewareEventMap);
                        break;
                    case RENAME_FILE:
                        new CommandRenameFileAction(ctx, byteBuf, this.middlewareEventMap);
                        break;
                    case DELETE_FILE:
                        new CommandDeleteFileAction(ctx, byteBuf, this.middlewareEventMap);
                        break;
                    default:
                        new ErrorAction(ctx, actionType, commandOptionType);
                }
                break;
            default:
                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN);
        }

        // FIX ByteBuf.release() was not called before it's garbage-collected.
        // FIX See http://netty.io/wiki/reference-counted-objects.html for more information.
        byteBuf.release();
    }
}

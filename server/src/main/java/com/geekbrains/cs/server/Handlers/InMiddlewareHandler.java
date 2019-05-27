package com.geekbrains.cs.server.Handlers;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.common.OptionTypes.*;
import com.geekbrains.cs.server.Middlewares.AbstractMiddleware;
import com.geekbrains.cs.server.Middlewares.AuthMiddleware;
import com.geekbrains.cs.server.Middlewares.MainMiddleware;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InMiddlewareHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    protected LinkedHashMap<HeaderType, String> headersMap = new LinkedHashMap<>();

    public LinkedHashMap<HeaderType, String> getHeadersMap() {
        return this.headersMap;
    }

    public void setHeadersMap(LinkedHashMap<HeaderType, String> headersMap) {
        this.headersMap = headersMap;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        LOGGER.log(Level.INFO, "{0} -> Client connected to middleware state", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.log(Level.INFO, "{0} -> Client disconnected from middleware state", ctx.channel().id());

        if (! (cause instanceof IOException) && ! (cause instanceof IndexOutOfBoundsException)) {
            cause.printStackTrace();
        }

        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte actionTypeByte = byteBuf.readByte();
        byte optionTypeByte = byteBuf.readByte();
        LOGGER.log(Level.INFO, "{0} -> action: {1}, option: {2}", new Object[] { ctx.channel().id(), actionTypeByte, optionTypeByte });

        ActionType actionType = ActionType.fromByte(actionTypeByte);

        InHandler inHandler = ctx.pipeline().get(InHandler.class);
        inHandler.setActionType(actionType);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);
                inHandler.setOptionType(accountOptionType);

                switch (accountOptionType) {
                    case SIGN_IN:
                    case SIGN_UP:
                        ctx.fireChannelRead(msg);
                        break;
                    case CHANGE_PASS:
                    case CHANGE_LOGIN:
                    case DELETE_ACCOUNT:
                        callMiddlewaresAndNextHandler(ctx, msg, new MainMiddleware(ctx, byteBuf), new AuthMiddleware(ctx, byteBuf));
                        break;
                    default:
                        ctx.fireChannelRead(msg);
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);
                inHandler.setOptionType(downloadOptionType);

                switch (downloadOptionType) {
                    case FILE:
                        callMiddlewaresAndNextHandler(ctx, msg, new MainMiddleware(ctx, byteBuf), new AuthMiddleware(ctx, byteBuf));
                        break;
                    default:
                        ctx.fireChannelRead(msg);
                }
                break;
            case UPLOAD:
                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);
                inHandler.setOptionType(uploadOptionType);

                switch (uploadOptionType) {
                    case FILE:
                        callMiddlewaresAndNextHandler(ctx, msg, new MainMiddleware(ctx, byteBuf), new AuthMiddleware(ctx, byteBuf));;
                        break;
                    default:
                        ctx.fireChannelRead(msg);
                }
                break;
            case COMMAND:
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);
                inHandler.setOptionType(commandOptionType);

                switch (commandOptionType) {
                    case FILE_LIST:
                    case RENAME_FILE:
                    case DELETE_FILE:
                        callMiddlewaresAndNextHandler(ctx, msg, new MainMiddleware(ctx, byteBuf), new AuthMiddleware(ctx, byteBuf));;
                        break;
                    default:
                        ctx.fireChannelRead(msg);
                }
                break;
            default:
                ctx.fireChannelRead(msg);
        }

        // FIX ByteBuf.release() was not called before it's garbage-collected.
        // FIX See http://netty.io/wiki/reference-counted-objects.html for more information.
//        byteBuf.release();
    }

    private void callMiddlewaresAndNextHandler(ChannelHandlerContext ctx, Object msg, AbstractMiddleware... middlewares) {
        boolean callMiddlewaresResult = callMiddlewares(middlewares);

        if (callMiddlewaresResult) {
            ctx.fireChannelRead(msg);
        }
    }

    private boolean callMiddlewares(AbstractMiddleware... middlewares) {
        for (AbstractMiddleware middleware : middlewares) {
            middleware.init();

            if (! middleware.canCallNext()) {
                return false;
            }
        }

        return true;
    }
}

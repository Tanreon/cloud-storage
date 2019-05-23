package com.geekbrains.cs.server.Handlers;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.*;
import com.geekbrains.cs.server.Actions.*;
import com.geekbrains.cs.server.Auth;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.NonStickyEventExecutorGroup;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private ActionType actionType;
    private OptionType optionType;
    private Auth auth;

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        LOGGER.log(Level.INFO, "{0} -> Client connected to in handler state", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.log(Level.INFO, "{0} -> Client disconnected from in handler state", ctx.channel().id());

        if (! (cause instanceof IOException) && ! (cause instanceof IndexOutOfBoundsException)) {
            cause.printStackTrace();
        }

        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = (AccountOptionType) this.optionType;

                switch (accountOptionType) {
                    case SIGN_IN:
                        Server.getExecutor().execute(() -> new AccountSignInAction(ctx, byteBuf));
                        break;
                    case SIGN_UP:
                        Server.getExecutor().execute(() -> new AccountSignUpAction(ctx, byteBuf));
                        break;
                    case CHANGE_PASS:
                    case CHANGE_LOGIN:
                    case DELETE_ACCOUNT:
                    default:
                        new ErrorAction(ctx, actionType, accountOptionType);
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = (DownloadOptionType) this.optionType;

                switch (downloadOptionType) {
                    case FILE:
                        Server.getExecutor().execute(() -> new DownloadFileAction(ctx, byteBuf, this.auth));
                        break;
                    default:
                        new ErrorAction(ctx, actionType, downloadOptionType);
                }
                break;
            case UPLOAD:
                UploadOptionType uploadOptionType = (UploadOptionType) this.optionType;

                switch (uploadOptionType) {
                    case FILE:
                        Server.getExecutor().execute(() -> new UploadFileAction(ctx, byteBuf, this.auth));
                        break;
                    default:
                        new ErrorAction(ctx, actionType, uploadOptionType);
                }
                break;
            case COMMAND:
                CommandOptionType commandOptionType = (CommandOptionType) this.optionType;

                switch (commandOptionType) {
                    case FILE_LIST:
                        Server.getExecutor().execute(() -> new CommandFileListAction(ctx, byteBuf, this.auth));
                        break;
                    case RENAME_FILE:
                        Server.getExecutor().execute(() -> new CommandRenameFileAction(ctx, byteBuf, this.auth));
                        break;
                    case DELETE_FILE:
                        Server.getExecutor().execute(() -> new CommandDeleteFileAction(ctx, byteBuf, this.auth));
                        break;
                    default:
                        new ErrorAction(ctx, actionType, commandOptionType);
                }
                break;
            default:
                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN);
        }
    }
}

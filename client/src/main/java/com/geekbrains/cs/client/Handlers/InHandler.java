package com.geekbrains.cs.client.Handlers;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Responses.*;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.OptionTypes.AccountOptionType;
import com.geekbrains.cs.common.OptionTypes.CommandOptionType;
import com.geekbrains.cs.common.OptionTypes.DownloadOptionType;
import com.geekbrains.cs.common.OptionTypes.UploadOptionType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        LOGGER.log(Level.INFO, "{0} -> Connected to server", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        byte actionTypeByte = byteBuf.readByte();
        byte optionTypeByte = byteBuf.readByte();
        LOGGER.log(Level.INFO, "action: {0}, option: {1}", new Object[]{actionTypeByte, optionTypeByte});

        ActionType actionType = ActionType.fromByte(actionTypeByte);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);

                switch (accountOptionType) {
                    case SIGN_IN:
                        new AccountSignInResponse(ctx, byteBuf);
                        break;
                    case SIGN_UP:
                        new AccountSignUpResponse(ctx, byteBuf);
                        break;
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);

                switch (downloadOptionType) {
                    case FILE:
                        new DownloadFileResponse(ctx, byteBuf);
                        break;
                }
                break;
            case UPLOAD:
                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);

                switch (uploadOptionType) {
                    case FILE:
                        new UploadFileResponse(ctx, byteBuf);
                        break;
                }
                break;
            case COMMAND:
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);

                switch (commandOptionType) {
                    case FILE_LIST:
                        new CommandFileListResponse(ctx, byteBuf);
                        break;
                    case RENAME_FILE:
                        new CommandRenameFileResponse(ctx, byteBuf);
                        break;
                    case DELETE_FILE:
                        new CommandDeleteFileResponse(ctx, byteBuf);
                        break;
                }
                break;
            default:
                LOGGER.log(Level.WARNING, "{0} -> Err response", ctx.channel().id());
        }

        // FIX ByteBuf.release() was not called before it's garbage-collected.
        // FIX See http://netty.io/wiki/reference-counted-objects.html for more information.
        byteBuf.release();
    }
}

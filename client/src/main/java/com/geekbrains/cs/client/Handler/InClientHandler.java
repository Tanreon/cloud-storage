package com.geekbrains.cs.client.Handler;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Response.*;
import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.OptionType.AccountOptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import com.geekbrains.cs.common.OptionType.DownloadOptionType;
import com.geekbrains.cs.common.OptionType.UploadOptionType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InClientHandler extends ChannelInboundHandlerAdapter {
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
//                    case CHANGE_PASS:
//                    case CHANGE_LOGIN:
//                    case DELETE_ACCOUNT:
//                    default:
//                        new ErrorAction(ctx, actionType, accountOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);

                switch (downloadOptionType) {
                    case FILE:
                        new DownloadFileResponse(ctx, byteBuf);
                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, downloadOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case UPLOAD:
                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);

                switch (uploadOptionType) {
                    case FILE:
                        new UploadFileResponse(ctx, byteBuf);
                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, uploadOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case COMMAND:
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);

                switch (commandOptionType) {
                    case FILE_LIST:
                        new CommandFileListResponse(ctx, byteBuf);
                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, commandOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
//            default:
//                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN, 404, "UNKNOWN_REQUEST");
//                LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());
        }
    }
}

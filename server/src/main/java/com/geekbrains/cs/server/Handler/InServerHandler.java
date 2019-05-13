package com.geekbrains.cs.server.Handler;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.OptionType.*;
import com.geekbrains.cs.server.Action.*;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        LOGGER.log(Level.INFO, "{0} -> Client connected", ctx.channel().id());
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
        LOGGER.log(Level.INFO, "action: {0}, option: {1}", new Object[] { actionTypeByte, optionTypeByte });

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
                        new ErrorAction(ctx, actionType, accountOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);

                switch (downloadOptionType) {
                    case FILE:
                        new DownloadFileAction(ctx, byteBuf);
                        break;
                    default:
                        new ErrorAction(ctx, actionType, downloadOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
//            case UPLOAD:
//                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);
//
//                switch (uploadOptionType) {
//                    case FILE:
//                        new UploadFileAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, uploadOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
            case COMMAND:
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);

                switch (commandOptionType) {
                    case FILE_LIST:
                        new CommandFileListAction(ctx, byteBuf);
                        break;
                    case RENAME_FILE:
                        new CommandRenameFileAction(ctx, byteBuf);
                        break;
                    case DELETE_FILE:
                        new CommandDeleteFileAction(ctx, byteBuf);
                        break;
                    default:
                        new ErrorAction(ctx, actionType, commandOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            default:
                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN, 404, "UNKNOWN_REQUEST");
                LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());
        }

        // ByteBuf.release() was not called before it's garbage-collected.
        // See http://netty.io/wiki/reference-counted-objects.html for more information.
        byteBuf.release();
    }
}

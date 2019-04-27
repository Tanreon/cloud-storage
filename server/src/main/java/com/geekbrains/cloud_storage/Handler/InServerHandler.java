package com.geekbrains.cloud_storage.Handler;

import com.geekbrains.cloud_storage.Action.AccountOptionType;
import com.geekbrains.cloud_storage.Action.AccountAuthRequest;
import com.geekbrains.cloud_storage.Action.CommandOptionType;
import com.geekbrains.cloud_storage.Action.CommandFileListRequest;
import com.geekbrains.cloud_storage.Action.DownloadFileRequest;
import com.geekbrains.cloud_storage.Action.DownloadOptionType;
import com.geekbrains.cloud_storage.Action.ErrorOptionType;
import com.geekbrains.cloud_storage.Action.ErrorRequest;
import com.geekbrains.cloud_storage.Action.UploadFileRequest;
import com.geekbrains.cloud_storage.Action.UploadOptionType;
import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.ByteArrayOutputStream;
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

        ByteBuf metaEndTest = Unpooled.buffer(4);
        byteBuf.readBytes(metaEndTest);

        if (metaEndTest.readByte() == (byte)0 && metaEndTest.readByte() == (byte)-1 && metaEndTest.readByte() == (byte)0 && metaEndTest.readByte() == (byte)-1) {
            LOGGER.log(Level.INFO, "meta end correct");
        } else {
            LOGGER.log(Level.INFO, "meta end NOT correct");
            throw new Exception();
        }

        ActionType actionType = ActionType.fromByte(actionTypeByte);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);
                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), accountOptionType} );

                switch (accountOptionType) {
                    case AUTH:
                        new AccountAuthRequest(ctx, byteBuf);
                        break;
                    case CHANGE_PASS:
                    case CHANGE_LOGIN:
                    case DELETE_ACCOUNT:
                    default:
                        new ErrorRequest(ctx, actionType, accountOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case DOWNLOAD:
                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);
                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), downloadOptionType} );

                switch (downloadOptionType) {
                    case FILE:
                        new DownloadFileRequest(ctx, byteBuf);
                        break;
                    default:
                        new ErrorRequest(ctx, actionType, downloadOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case UPLOAD:
                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);
                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), uploadOptionType} );

                switch (uploadOptionType) {
                    case FILE:
                        new UploadFileRequest(ctx, byteBuf);
                        break;
                    default:
                        new ErrorRequest(ctx, actionType, uploadOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            case COMMAND:
                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);
                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), commandOptionType} );

                switch (commandOptionType) {
                    case FILE_LIST:
                        new CommandFileListRequest(ctx, byteBuf);
                        break;
                    default:
                        new ErrorRequest(ctx, actionType, commandOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
            default:
                new ErrorRequest(ctx, actionType, ErrorOptionType.UNKNOWN, 404, "UNKNOWN_REQUEST");
                LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());
        }
    }
}

package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Contract.OptionType;
import com.geekbrains.cloud_storage.Response;
import com.geekbrains.cloud_storage.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandFileListRequest {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.FILE_LIST;

    ////////////////////
    private String login;
    ////////////////////

    private ChannelHandlerContext ctx;
    private ByteBuf message;

    public CommandFileListRequest(ChannelHandlerContext ctx, ByteBuf message) {
        this.ctx = ctx;
        this.message = message;

        ////////////////////
        this.login = "test";
        ////////////////////

        // Run protocol processing
        this.receiveDataByProtocol();
        // Run request processing
        this.run();
    }

    private void run() {
        Path storage = Paths.get("storage", this.login);

        Stream<Path> files;

        try {
            files = Files.list(storage);
        } catch (NoSuchFileException e) {
            LOGGER.log(Level.WARNING, "{0} -> Path not found err", ctx.channel().id());
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            ctx.close();
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "{0} -> IOException when reading client dir", ctx.channel().id());
            e.printStackTrace();
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            ctx.close();
            return;
        }

//        ctx.writeAndFlush(generatePacketByProtocol(files.collect(Collectors.toList())));
        generatePacketByProtocol(files.collect(Collectors.toList()));

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

//    private void generatePacketByProtocol(List<Path> filesList) {
    private void generatePacketByProtocol(List<Path> filesListx) {
        List<String> filesList = new LinkedList<>();

        for (int i = 0; i < 30; i++) {
            filesList.add(String.format("file_%d.mp4", i));
        }

        ByteBuf metaBuf = Unpooled.wrappedBuffer(
            Unpooled.buffer(1).writeByte(ACTION_TYPE.getValue()),
            Unpooled.buffer(1).writeByte(OPTION_TYPE.getValue()),
            Unpooled.copyShort(200),
            Unpooled.wrappedBuffer(new byte[] { (byte)0, (byte)-1, (byte)0, (byte)-1 })
        );

        ctx.write(metaBuf);

//        filesList.forEach(path -> { // another data
//            byte[] fileNameBytes = path.getFileName().toString().getBytes();
//
//            ByteBuf dataBuf = Unpooled.wrappedBuffer(
//                Unpooled.copyInt(fileNameBytes.length),
//                Unpooled.copiedBuffer(fileNameBytes)
//            );
//
//            ctx.write(dataBuf);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });

        filesList.forEach(item -> { // another data
            byte[] fileNameBytes = item.getBytes();

            ByteBuf dataBuf = Unpooled.wrappedBuffer(
                Unpooled.copyShort(fileNameBytes.length),
                Unpooled.copiedBuffer(fileNameBytes)
            );

            System.out.println("write " + item);

            ctx.write(Unpooled.wrappedBuffer(dataBuf));
        });

        ctx.write(Unpooled.wrappedBuffer(new byte[] { (byte)0, (byte)-1, (byte)0, (byte)-1 }));
        ctx.flush();
    }

    private void receiveDataByProtocol() {
//        if (! message.isReadable()) {
//            LOGGER.log(Level.WARNING, "{0} -> Protocol receiving err", ctx.channel().id());
//            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
//            ctx.close();
//            return;
//        }
//
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

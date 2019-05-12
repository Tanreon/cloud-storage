package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Contract.OptionType;
import com.geekbrains.cloud_storage.Response;
import com.geekbrains.cloud_storage.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CommandFileListAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final String STORAGE_PATH = "server_storage";

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.FILE_LIST;

    ////////////////////
    private String login = "test";
    ////////////////////

    private Stream<Path> fileStream;

    public CommandFileListAction(ChannelHandlerContext ctx, ByteBuf message) throws Exception {
        this.ctx = ctx;
        this.message = message;

        // Run protocol request processing
        if (!this.receiveDataByProtocol()) {
            return;
        }

        // Run request processing
        if (!this.run()) {
            return;
        }

        // Run protocol answer processing
        if (!this.sendDataByProtocol()) {
            return;
        }
    }

    /*
     * TODO добавить возможность чтения только 1 каталога
     * т.е. что бы создать структуру дерева файлов нужно для каждого каталога делать отдельный запрос
     * */
    @Override
    protected boolean receiveDataByProtocol() {
        return true;
    }

    @Override
    protected boolean run() {
        Path storage = Paths.get(STORAGE_PATH, this.login);

        try {
            this.fileStream = Files.list(storage);
        } catch (NoSuchFileException e) {
            LOGGER.log(Level.WARNING, "{0} -> Path not found err", ctx.channel().id());

            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            return false;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "{0} -> IOException when reading client dir", ctx.channel().id());

            e.printStackTrace();
            ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            return false;
        }

        return true;
    }

    @Override
    protected boolean sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> File list command success: {1}", new Object[]{this.ctx.channel().id(), this.login});

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        this.fileStream.forEach(item -> {
            try {
                outputStream.write(this.resp(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK", false))); // FIXME возможно нет ничего плохого в том что делается два write на сокете. Дублирование кода
            } catch (IOException e) {
                e.printStackTrace();
                ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            }

            byte[] nameBytes = item.toFile().getName().getBytes();
            long size = item.toFile().length();

            long createdAt;
            long modifiedAt;

            try {
                createdAt = Files.readAttributes(item, BasicFileAttributes.class).creationTime().toMillis();
                modifiedAt = Files.readAttributes(item, BasicFileAttributes.class).lastModifiedTime().toMillis();
            } catch (IOException e) {
                createdAt = System.currentTimeMillis();
                modifiedAt = System.currentTimeMillis();
            }

            try {
                outputStream.writeInt(nameBytes.length);
                outputStream.write(nameBytes);
                outputStream.writeLong(size);
                outputStream.writeLong(createdAt);
                outputStream.writeLong(modifiedAt);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                outputStream.write(new byte[]{(byte) 0, (byte) -1});
            } catch (IOException e) {
                e.printStackTrace();
                ctx.writeAndFlush(new Response(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
            }

            this.ctx.writeAndFlush(Unpooled.wrappedBuffer(byteOutputStream.toByteArray()));

            byteOutputStream.reset();
        });

        return true;
    }
}

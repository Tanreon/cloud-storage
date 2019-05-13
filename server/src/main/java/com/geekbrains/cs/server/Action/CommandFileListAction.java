package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.OptionType.CommandOptionType;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

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

    public CommandFileListAction(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

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
        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            return false;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }

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

        this.fileStream.forEach(item -> {
            {
                ctx.write(new Response(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
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

            ByteBuf byteBuf = Unpooled.directBuffer();

            byteBuf.writeInt(nameBytes.length);
            byteBuf.writeBytes(nameBytes);
            byteBuf.writeLong(size);
            byteBuf.writeLong(createdAt);
            byteBuf.writeLong(modifiedAt);
            byteBuf.writeBytes(Server.getEndBytes());

            this.ctx.writeAndFlush(byteBuf);
        });

        return true;
    }
}

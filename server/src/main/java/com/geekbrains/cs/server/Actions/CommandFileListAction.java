package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.OptionType;
import com.geekbrains.cs.common.OptionTypes.CommandOptionType;
import com.geekbrains.cs.server.ActionResponse;
import com.geekbrains.cs.server.Contracts.ProcessFailureException;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommandFileListAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final ActionType ACTION_TYPE = ActionType.COMMAND;
    private final OptionType OPTION_TYPE = CommandOptionType.FILE_LIST;

    ////////////////////
    private String login = "test";
    ////////////////////

    private List<Path> fileList;

    public CommandFileListAction(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
            // Run protocol answer processing
            this.sendDataByProtocol();
        } catch (IndexOutOfBoundsException ex) {
            LOGGER.log(Level.INFO, "{0} -> IndexOutOfBoundsException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.INFO, "{0} -> IncorrectEndException", ctx.channel().id());
            ctx.writeAndFlush(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 400, "BAD_REQUEST"));
        } catch (ProcessFailureException ex) {
            LOGGER.log(Level.WARNING, "{0} -> ProcessFailureException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            ctx.writeAndFlush(ex.getResponse());
        }
    }

    /**
     * protocol: []
     *
     * TODO добавить возможность чтения только 1 каталога
     * TODO т.е. что бы создать структуру дерева файлов нужно для каждого каталога делать отдельный запрос
     * */
    @Override
    protected void receiveDataByProtocol() throws IncorrectEndException {
        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessFailureException {
        Path storage = Paths.get(Server.STORAGE_PATH, this.login);

        try {
            this.fileList = Files.list(storage).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new ProcessFailureException(ex, new ActionResponse(ACTION_TYPE, OPTION_TYPE, 500, "SERVER_ERROR"));
        }
    }

    /**
     * protocol: [RESPONSE][filesCount][fileNameLength][fileNameBytes][fileSize][fileCreatedAt][fileModifiedAt][END]
     * */
    @Override
    protected void sendDataByProtocol() {
        LOGGER.log(Level.INFO, "{0} -> File list command success: {1}", new Object[]{this.ctx.channel().id(), this.login});

        if (this.fileList.size() == 0) {
            { // write head
                ctx.write(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
            }

            { // write files count
                ctx.write(0);
            }

            { // write end
                this.writeEndBytes();
            }

            this.ctx.flush();
        } else {
            for (int i = 0; i < this.fileList.size(); i++) {
                Path path = this.fileList.get(i);
                File file = path.toFile();

                LOGGER.log(Level.INFO, "{0} --> Sending file list part, file: {1}", new Object[]{ this.ctx.channel().id(), file.getName() });

                { // write head
                    ctx.write(new ActionResponse(ACTION_TYPE, OPTION_TYPE, 200, "OK", false));
                }

                { // write files count
                    ctx.write(this.fileList.size());
                }

                { // write current file index
                    ctx.write(i);
                }

                { // write file name
                    this.writeStringByShort(file.getName());
                }

                { // write file size
                    this.ctx.write(file.length());
                }

                { // write created date
                    long createdAt = Instant.EPOCH.toEpochMilli();

                    try {
                        createdAt = Files.readAttributes(path, BasicFileAttributes.class).creationTime().toMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ctx.write(createdAt);
                }

                { // write modified date
                    long modifiedAt = Instant.EPOCH.toEpochMilli();

                    try {
                        modifiedAt = Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toMillis();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ctx.write(modifiedAt);
                }

                { // write end
                    this.writeEndBytes();
                }

                this.ctx.flush();
            }
        }
    }
}

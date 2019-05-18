package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Controllers.MainController;
import com.geekbrains.cs.common.Contracts.EmptyResponseException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandFileListResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private int filesCount;
    private int currentFileIndex;
    private String fileName;
    private long fileSize;
    private long fileCreatedAt;
    private long fileModifiedAt;

    public CommandFileListResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Список файлов", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Список файлов", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Список файлов", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][filesCount][fileNameLength][fileNameBytes][fileSize][fileCreatedAt][fileModifiedAt][END]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyResponseException, IncorrectEndException {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            throw new EmptyResponseException();
        }

        { // read files count
            this.filesCount = this.byteBuf.readInt();
        }

        if (this.filesCount > 0) {
            { // read current file index
                this.currentFileIndex = this.byteBuf.readInt();
            }

            { // read name
                this.fileName = this.readStringByShort();
            }

            { // read size
                this.fileSize = this.byteBuf.readLong();
            }

            { // read created date
                this.fileCreatedAt = this.byteBuf.readLong();
            }

            { // read modified date
                this.fileModifiedAt = this.byteBuf.readLong();
            }
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> {
                MainController mainController = (MainController) gui.getMainStage().getUserData();

                if (this.filesCount == 0) {
                    mainController.getServerStorageFileRowList().clear();
                } else {
                    if (this.currentFileIndex == 0) {
                        mainController.getServerStorageFileRowList().clear();
                    }

                    MainController.ServerFileRow serverFileRow = new MainController.ServerFileRow();
                    serverFileRow.setName(this.fileName);
                    serverFileRow.setSize(this.fileSize);
                    serverFileRow.setModifiedAt(this.fileModifiedAt);
                    serverFileRow.setAvailability(100); // FIXME сохранять предыдущее значение в конфиг либо в filename.conf.tmp

                    mainController.getServerStorageFileRowList().add(serverFileRow);
                }
            });
        } else {
            switch (this.message) {
                case "SERVER_ERROR":
                    throw new ProcessException(this.status, "Ошибка на стороне сервера, попробуйте позже");
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

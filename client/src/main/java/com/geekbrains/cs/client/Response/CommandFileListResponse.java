package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Controller.MainController;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandFileListResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private MainController.ServerFileRow fileRow;
    private int filesCount;

    public CommandFileListResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) throws IOException {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        // Run protocol response processing
        if (! this.receiveDataByProtocol()) {
            return;
        }

        // Run response processing
        if (! this.run()) {
            return;
        }
    }

    @Override
    protected boolean receiveDataByProtocol() throws IOException {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            LOGGER.log(Level.INFO, "Ошибка, мета информация не доступна");

            return false;
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return true;
        }

        { // read files count
            this.filesCount = this.byteBuf.readInt();
        }

        MainController.ServerFileRow fileRow = new MainController.ServerFileRow();

        { // read name
            fileRow.setName(this.readStringByInt());
        }

        { // read size
            fileRow.setSize(this.byteBuf.readLong());
        }

        { // read created at
            fileRow.setCreatedAt(this.byteBuf.readLong());
        }

        { // read updated at
            fileRow.setModifiedAt(this.byteBuf.readLong());
        }

        this.fileRow = fileRow;

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new IOException("End bytes not received");
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }

        return true;
    }

    @Override
    protected boolean run() {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> {
                MainController mainController = (MainController) gui.getMainStage().getUserData();

                if (this.filesCount == mainController.getServerStorageTableView().getItems().size()) {
                    mainController.getServerStorageTableView().getItems().clear();
                }

                mainController.getServerStorageTableView().getItems().add(this.fileRow);
            });
        } else {
            switch (this.message) { // TODO дополнительные ошибки
                case "SERVER_ERROR":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Список файлов", "Ошибка на стороне сервера, попробуйте позже."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Список файлов", "Неизвестная ошибка, попробуйте позже."));
            }
        }

        return true;
    }
}

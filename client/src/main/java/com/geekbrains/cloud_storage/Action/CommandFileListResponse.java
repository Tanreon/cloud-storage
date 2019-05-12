package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.MainController;
import com.geekbrains.cloud_storage.ServerFileRow;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandFileListResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private ServerFileRow fileRow;

    public CommandFileListResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void run() {
        if (this.status == 200) {
            MainController mainController = (MainController) Client.getGui().getMainStage().getUserData();
            mainController.getServerStorageTableView().getItems().add(this.fileRow);
        } else {
            switch (this.message) { // TODO дополнительные ошибки
                case "SERVER_ERROR":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Список файлов", "Ошибка на стороне сервера, попробуйте позже."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Список файлов", "Неизвестная ошибка, попробуйте позже."));
            }
        }
    }

    protected void receiveDataByProtocol() throws Exception {
        this.readMeta();

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return;
        }

        ServerFileRow fileRow = new ServerFileRow();

        { // read name
            fileRow.setName(this.readString());
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

            throw new Exception("End bytes not received");
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }
    }
}

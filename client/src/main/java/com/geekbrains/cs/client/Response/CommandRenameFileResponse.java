package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Controller.MainController;
import com.geekbrains.cs.client.Request.CommandFileListRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandRenameFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public CommandRenameFileResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void receiveDataByProtocol() {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            LOGGER.log(Level.INFO, "Ошибка, мета информация не доступна");

            return;
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            return;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }
    }

    protected void run() {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> ((MainController) gui.getMainStage().getUserData()).updateServerStorageTableView(true));
        } else {
            switch (this.message) { // TODO дополнительные ошибки
                case "OLD_FILE_NOT_FOUND":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Переименование", "Файл не найден."));
                    break;
                case "NEW_FILE_EXISTS":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Переименование", "Файл с таким именем уже существует."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Переименование", "Неизвестная ошибка, попробуйте позже."));
            }
        }
    }
}

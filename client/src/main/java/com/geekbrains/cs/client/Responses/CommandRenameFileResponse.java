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

public class CommandRenameFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public CommandRenameFileResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Переименование файла", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Переименование файла", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Переименование файла", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][END]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyResponseException, IncorrectEndException {
        if (this.inByteBuf.isReadable()) {
            this.readMeta();
        } else {
            throw new EmptyResponseException();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> ((MainController) gui.getMainStage().getUserData()).updateServerStorageTableView(true));
        } else {
            switch (this.message) {
                case "CURRENT_FILE_NOT_FOUND":
                    throw new ProcessException(this.status, "Файл не найден");
                case "NEW_FILE_EXISTS":
                    throw new ProcessException(this.status, "Файл с таким именем уже существует");
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

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

public class CommandDeleteFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public CommandDeleteFileResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Удаление файла", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Удаление файла", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Удаление файла", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][END]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyResponseException, IncorrectEndException {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            throw new EmptyResponseException();
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> ((MainController) gui.getMainStage().getUserData()).updateServerStorageTableView(true));
        } else {
            switch (this.message) {
                case "FILE_NOT_FOUND":
                    throw new ProcessException(this.status, "Файл не найден");
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

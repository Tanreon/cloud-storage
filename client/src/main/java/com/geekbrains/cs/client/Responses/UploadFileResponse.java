package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Controllers.MainController;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.EmptyResponseException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UploadFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private String fileName;
    private long filePartsCount;
    private int fileCurrentPart;

    public UploadFileResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Закачивание", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Закачивание", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Закачивание", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][fileNamePartsCount][fileNameCurrentPart][END]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyResponseException, IncorrectEndException {
        if (this.inByteBuf.isReadable()) {
            this.readMeta();
        } else {
            throw new EmptyResponseException();
        }

        { // get filename
            this.fileName = this.readStringByShort();
        }

        { // get full file size in Common.BUFFER_LENGTH bytes block
            this.filePartsCount = this.inByteBuf.readLong();
        }

        { // get file part
            this.fileCurrentPart = this.inByteBuf.readInt();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        if (this.status == 200) {
            double fileAvailability = (double) (this.fileCurrentPart + 1) / this.filePartsCount * 100;

            Client.getGui().runInThread(gui -> {
                MainController mainController = (MainController) gui.getMainStage().getUserData();
                mainController.updateServerStorageTableView(this.fileCurrentPart == 0 || fileAvailability == 100);
                mainController.getServerStorageFileRowList().stream()
                    .filter(item -> item.getName().equals(this.fileName))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setSize(this.fileCurrentPart * Common.BUFFER_LENGTH);
                        item.setAvailability((int) Math.round(fileAvailability));
                    });
            });
        } else {
            switch (this.message) {
                case "BAD_REQUEST":
                case "SERVER_ERROR":
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

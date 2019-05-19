package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.client.Controllers.MainController;
import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.common.Contracts.EmptyResponseException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private String fileName;
    private long filePartsCount;
    private int fileCurrentPart;
    private byte[] fileDataBytes;

    public DownloadFileResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Скачивание", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Скачивание", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Скачивание", ex.getMessage()));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "IOException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Скачивание", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][fileNameLength][fileNameBytes][filePartsCount][currentFilePart][fileReadLength][fileReadBytes][END]
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

        { // get data
            this.fileDataBytes = this.readBytesByLong();
        }

        if (this.inByteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws IOException, ProcessException {
        if (this.status == 200) {
            LOGGER.log(Level.INFO, "Запись части файла: filename {0}, part {1}", new Object[]{ this.fileName, this.fileCurrentPart});
            File file = Paths.get(Client.STORAGE_PATH, this.fileName).toFile();

            if (this.fileCurrentPart == 0 && file.exists()) {
                file.delete();
            }

            while (true) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                    randomAccessFile.seek(this.fileCurrentPart * Common.BUFFER_LENGTH);
                    randomAccessFile.write(this.fileDataBytes);

                    break;
                } catch (FileNotFoundException ignored) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            double fileAvailability = (double) (this.fileCurrentPart + 1) / this.filePartsCount * 100;

            Client.getGui().runInThread(gui -> {
                MainController mainController = (MainController) gui.getMainStage().getUserData();
                mainController.updateClientStorageTableView(this.fileCurrentPart == 0 || fileAvailability == 100);
                mainController.getClientStorageFileRowList().stream()
                        .filter(item -> item.getName().equals(this.fileName))
                        .findFirst()
                        .ifPresent(item -> {
                            item.setSize(this.fileCurrentPart * Common.BUFFER_LENGTH);
                            item.setAvailability((int) Math.round(fileAvailability));
                        });
            });
        } else {
            switch (this.message) {
                case "FILE_NOT_FOUND":
                    throw new ProcessException(this.status, "Файл не найден");
                case "BAD_REQUEST":
                case "SERVER_ERROR":
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

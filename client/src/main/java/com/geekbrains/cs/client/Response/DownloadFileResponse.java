package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.client.Client;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final String STORAGE_PATH = "client_storage";

    private String fileName;
    private long fileDataBlocksCount;
    private int fileDataPart;
    private byte[] fileDataBytes;

    public DownloadFileResponse(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        this.ctx = ctx;
        this.byteBuf = msg;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void receiveDataByProtocol() throws Exception {
        this.readMeta();

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return;
        }

        { // get filename
            this.fileName = this.readString();
        }

        { // get full file size in 4096 bytes block
            this.fileDataBlocksCount = this.byteBuf.readLong();
        }

        { // get file part
            this.fileDataPart = this.byteBuf.readInt();
        }

        { // get data
            this.fileDataBytes = this.readBytes();
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }
    }

    protected void run() throws IOException {
        if (this.status == 200) {
            LOGGER.log(Level.INFO, "Запись части файла: filename {0}, part {1}", new Object[]{this.fileName, this.fileDataPart});
            File file = Paths.get(STORAGE_PATH, this.fileName).toFile();

            if (this.fileDataPart == 1 && file.exists()) {
                file.delete();
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.write(this.fileDataBytes);
            randomAccessFile.close();
        } else {
            switch (this.message) { // TODO дополнительные ошибки
                case "FILE_NOT_FOUND":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Скачивание", "Файл не найден."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Скачивание", "Неизвестная ошибка, попробуйте позже."));
            }
        }
    }
}

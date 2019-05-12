package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.Contract.OptionType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadFileResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final String STORAGE_PATH = "client_storage";

    private short status;
    private String message;
    private String fileName;
    private long fileSizeByBlocks;
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
        { // readResponse status
            this.status = this.byteBuf.readShort();
        }

        { // readResponse message
            int messageLength = this.byteBuf.readInt();

            if (messageLength != 0) {
                byte[] messageBytes = new byte[messageLength];
                this.byteBuf.readBytes(messageBytes);
                this.message = new String(messageBytes);
            }
        }

        if (this.isResponseEndReached(this.byteBuf)) { // check end
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        }

        { // get filename
            int fileNameLength = this.byteBuf.readInt();

            byte[] fileNameBytes = new byte[fileNameLength];
            this.byteBuf.readBytes(fileNameBytes);
            this.fileName = new String(fileNameBytes);
        }

        { // get full file size in 4096 bytes block
            this.fileSizeByBlocks = this.byteBuf.readLong();
        }

        { // get data by part
            this.fileDataPart = this.byteBuf.readInt();

            int fileDataLength = this.byteBuf.readInt();
            this.fileDataBytes = new byte[fileDataLength];
            this.byteBuf.readBytes(fileDataBytes);
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
        }
    }

    protected void run() throws IOException {
        if (this.status == 200) {
            LOGGER.log(Level.INFO, "Запись части файла: filename {0}, part {1}", new Object[] { this.fileName, this.fileDataPart });
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

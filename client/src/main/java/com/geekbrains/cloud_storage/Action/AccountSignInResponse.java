package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.Contract.OptionType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_UP;

    private short status;
    private String message;
    private String key;

    public AccountSignInResponse(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        this.ctx = ctx;
        this.msg = msg;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    public AccountSignInResponse(SocketChannel socketChannel) throws Exception {
        this.socketChannel = socketChannel;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void receiveDataByProtocol() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.readResponse().toByteArray());
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

        { // readResponse status
            this.status = this.msg.readShort();
        }

        { // readResponse message
            int messageLength = this.msg.readInt();

            if (messageLength != 0) {
                byte[] messageBytes = new byte[messageLength];
                this.msg.readBytes(messageBytes);
                this.message = new String(messageBytes);
            }
        }

        if (this.isResponseEndReached(this.msg)) { // check end
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return;
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        }

        { // get key
            int keyLength = this.msg.readInt();

            byte[] keyBytes = new byte[keyLength];
            this.msg.readBytes(keyBytes);
            this.key = new String(keyBytes);
        }

        if (this.isResponseEndReached(this.msg)) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
        }
    }

//    protected void receiveDataByProtocol() throws Exception {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.readResponse().toByteArray());
//        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);
//
//        { // readResponse status
//            this.status = inputStream.readShort();
//        }
//
//        { // readResponse message
//            int messageLength = inputStream.readInt();
//
//            if (messageLength != 0) {
//                byte[] messageBytes = new byte[messageLength];
//                inputStream.read(messageBytes);
//                this.message = new String(messageBytes);
//            }
//        }
//
//        if (this.isResponseEndReached(byteArrayInputStream)) { // check end
//            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");
//
//            return;
//        } else {
//            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
//        }
//
//        { // get key
//            int keyLength = inputStream.readInt();
//
//            byte[] keyBytes = new byte[keyLength];
//            inputStream.read(keyBytes);
//            this.key = new String(keyBytes);
//        }
//
//        if (this.isResponseEndReached(byteArrayInputStream)) { // check end
//            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
//        } else {
//            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");
//
//            throw new Exception("End bytes not received");
//        }
//    }

    protected void run() {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> {
                gui.getMainStage().show();
                gui.getSignInStage().close();
            });

            Client.getAuth().setKey(this.key);
        } else {
            switch (this.message) { // TODO дополнительные ошибки
                case "LOGIN_NOT_FOUND":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", "Логин не найден."));
                    break;
                case "CREDENTIALS_NOT_CORRECT":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", "Логин или пароль не корректны."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", "Неизвестная ошибка, попробуйте позже."));
            }
        }
    }
}

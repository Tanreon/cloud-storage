package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.Contract.OptionType;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final ActionType ACTION_TYPE = ActionType.ACCOUNT;
    private final OptionType OPTION_TYPE = AccountOptionType.SIGN_UP;

    private short status;
    private String message;

    public AccountSignUpResponse(SocketChannel socketChannel) throws Exception {
        this.socketChannel = socketChannel;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void run() {
        if (this.status == 200) {
            Client.getGui().runInThread(gui -> {
                gui.showInfoAlert("Успех", "Регистрация", "Вы успешно зарегистрировались.");

                gui.getSignUpStage().show();
            });
        } else {
            switch (this.message) {
                case "LOGIN_ALREADY_EXISTS":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Регистрация", "Такой логин уже существует."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Регистрация", "Неизвестная ошибка, попробуйте позже."));
            }
        }
    }

    protected void receiveDataByProtocol() throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.readResponse().toByteArray());
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

        { // readResponse status
            this.status = inputStream.readShort();
        }

        { // readResponse message
            int messageLength = inputStream.readInt();

            if (messageLength != 0) {
                byte[] messageBytes = new byte[messageLength];
                inputStream.read(messageBytes);
                this.message = new String(messageBytes);
            }
        }

        if (this.isResponseEndReached(byteArrayInputStream)) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
        }
    }
}

package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.client.Client;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private String key;

    public AccountSignInResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) throws IOException {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        // Run protocol response processing
        if (! this.receiveDataByProtocol()) {
            return;
        }

        // Run response processing
        if (! this.run()) {
            return;
        }
    }

    @Override
    protected boolean receiveDataByProtocol() throws IOException {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            LOGGER.log(Level.INFO, "Ошибка, мета информация не доступна");

            return false;
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Данные корректны, продолжаем чтение...");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, нет доступных данных для чтения подробнее в сообщении");

            return true;
        }

        { // get key
            this.key = this.readStringByInt();
        }

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new IOException("End bytes not received");
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }

        return true;
    }

    @Override
    protected boolean run() {
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
                case "LOGIN_LENGTH_SMALL":
                case "PASSWORD_LENGTH_SMALL":
                case "CREDENTIALS_NOT_CORRECT":
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", "Логин или пароль не корректны."));
                    break;
                default:
                    Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", "Неизвестная ошибка, попробуйте позже."));
            }
        }

        return true;
    }
}

package com.geekbrains.cs.client.Response;

import com.geekbrains.cs.client.Client;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public AccountSignUpResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) throws IOException {
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

        return true;
    }
}

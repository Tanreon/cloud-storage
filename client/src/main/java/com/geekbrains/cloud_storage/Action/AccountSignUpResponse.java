package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Client;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignUpResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public AccountSignUpResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    protected void receiveDataByProtocol() throws Exception {
        this.readMeta();

        if (this.byteBuf.isReadable()) { // check end
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
        } else {
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        }
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
}

package com.geekbrains.cs.client.Responses;

import com.geekbrains.cs.client.Client;
import com.geekbrains.cs.common.Contracts.EmptyResponseException;
import com.geekbrains.cs.common.Contracts.IncorrectEndException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AccountSignInResponse extends AbstractResponse {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private String key;

    public AccountSignInResponse(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.byteBuf = byteBuf;

        try {
            // Run protocol response processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyResponseException ex) {
            LOGGER.log(Level.WARNING, "EmptyResponseException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Вход", "Пустой ответ от сервера"));
        } catch (IncorrectEndException ex) {
            LOGGER.log(Level.WARNING, "IncorrectEndException: {0}");
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Критическая ошибка", "Вход", "Получено больше данных чем требовалось"));
        } catch (ProcessException ex) {
            LOGGER.log(Level.WARNING, "ProcessException: {0}", ex.getMessage());
            Client.getGui().runInThread(gui -> gui.showErrorAlert("Ошибка", "Вход", ex.getMessage()));
        }
    }

    /**
     * protocol: [RESPONSE][keyLength][keyBytes][END]
     * */
    @Override
    protected void receiveDataByProtocol() throws EmptyResponseException, IncorrectEndException {
        if (this.byteBuf.isReadable()) {
            this.readMeta();
        } else {
            throw new EmptyResponseException();
        }

        { // get key
            this.key = this.readStringByShort();
        }

        if (this.byteBuf.isReadable()) { // check end
            throw new IncorrectEndException();
        }
    }

    @Override
    protected void process() throws ProcessException {
        if (this.status == 200) {
            Client.getAuth().setKey(this.key);

            Client.getGui().runInThread(gui -> {
                gui.getMainStage().show();
                gui.getSignInStage().close();
            });
        } else {
            switch (this.message) {
                case "LOGIN_NOT_FOUND":
                    throw new ProcessException(this.status, "Логин не найден");
                case "LOGIN_LENGTH_SMALL":
                    throw new ProcessException(this.status, "Логин слишком короткий");
                case "PASSWORD_LENGTH_SMALL":
                    throw new ProcessException(this.status, "Пароль слишком короткий");
                case "CREDENTIALS_NOT_CORRECT":
                    throw new ProcessException(this.status, "Логин или пароль не подходят");
                default:
                    throw new ProcessException(this.status, "Неизвестная ошибка, попробуйте позже");
            }
        }
    }
}

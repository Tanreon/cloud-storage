package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.ActionType;
import com.geekbrains.cloud_storage.Client;
import com.geekbrains.cloud_storage.Contract.OptionType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

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

    public AccountSignUpResponse(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        this.ctx = ctx;
        this.msg = msg;

        // Run protocol request processing
        this.receiveDataByProtocol();

        // Run request processing
        this.run();
    }

    public AccountSignUpResponse(SocketChannel socketChannel) throws Exception {
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
            LOGGER.log(Level.INFO, "Данные корректны, завершаем чтение");
        } else {
            LOGGER.log(Level.INFO, "Ошибка, не получен завершающий байт");

            throw new Exception("End bytes not received");
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

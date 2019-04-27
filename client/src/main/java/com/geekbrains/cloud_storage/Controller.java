package com.geekbrains.cloud_storage;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controller {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    public void sendAuth(ActionEvent actionEvent) throws IOException {
        Client.getNetwork().getSocketChannel().write(ByteBuffer.wrap(new byte[]{(byte) 90, (byte) 1}));
        Client.getNetwork().getSocketChannel().write(ByteBuffer.wrap(new byte[]{ (byte) 0, (byte) -1, (byte) 0, (byte) -1 })); // send end
        LOGGER.log(Level.INFO, "sending: new byte[] { (byte) 90, (byte) 1 }");
    }

    public void sendMsg(ActionEvent actionEvent) {

    }
}

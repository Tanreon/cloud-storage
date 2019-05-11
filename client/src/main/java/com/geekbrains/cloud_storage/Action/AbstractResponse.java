package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public abstract class AbstractResponse {
    protected SocketChannel socketChannel;

    protected abstract void run() throws Exception;
    protected abstract void receiveDataByProtocol() throws Exception;

    protected ByteArrayOutputStream readResponse() throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(8192);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        while (this.socketChannel.read(dataBuffer) > 1) {
            dataBuffer.flip();

            byte[] buffer = new byte[dataBuffer.remaining()];
            dataBuffer.get(buffer);
            outputStream.write(buffer);

            dataBuffer.flip();
        }

        return byteOutputStream;
    }

    protected boolean isResponseEndReached(ByteArrayInputStream inputStream) throws IOException {
        if (inputStream.available() < 2) {
            throw new IOException("Нет данных для чтения");
        }

        inputStream.mark(2);

        byte[] dataEndBytes = new byte[2];
        inputStream.read(dataEndBytes);

        inputStream.reset();

        return Arrays.equals(dataEndBytes, Client.getNetwork().getEndBytes());
    }
}

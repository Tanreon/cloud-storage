package com.geekbrains.cloud_storage.Action;

import com.geekbrains.cloud_storage.Client;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public abstract class AbstractResponse {
    protected SocketChannel socketChannel;
    protected ChannelHandlerContext ctx;
    protected ByteBuf msg;

    protected abstract void run() throws Exception;
    protected abstract void receiveDataByProtocol() throws Exception;

//    protected ByteArrayOutputStream readResponse() throws IOException {
//        ByteBuffer dataBuffer = ByteBuffer.allocate(8192);
//
//        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
//        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);
//
//        while (this.ctx.read(dataBuffer) > 1) {
//            dataBuffer.flip();
//
//            byte[] buffer = new byte[dataBuffer.remaining()];
//            dataBuffer.get(buffer);
//            outputStream.write(buffer);
//
//            dataBuffer.flip();
//        }
//
//        return byteOutputStream;
//    }

    protected ByteArrayOutputStream readResponse() throws IOException {
//        if (! message.isReadable()) {
//            throw new Exception("Empty data");
//        }

        ByteBuffer dataBuffer = ByteBuffer.allocate(8192);

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

//        while (this.ctx.read() > 1) {
//            dataBuffer.flip();
//
//            byte[] buffer = new byte[dataBuffer.remaining()];
//            dataBuffer.get(buffer);
//            outputStream.write(buffer);
//
//            dataBuffer.flip();
//        }

        return byteOutputStream;
    }

    protected ByteArrayOutputStream readResponse(ByteBuf byteBuf) throws IOException {
        if (! byteBuf.isReadable()) {
            throw new IOException("Empty data");
        }

        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);

        ByteBufAllocator alloc = this.ctx.read().alloc();
        ByteBuf allocB = alloc.buffer();

        while (allocB.readableBytes() > 1) {
            byte[] buffer = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(buffer);
            outputStream.write(buffer);
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

        return Arrays.equals(dataEndBytes, Client.getEndBytes());
    }

    protected boolean isResponseEndReached(ByteBuf byteBuf) throws IOException { // FIXME избыточность, убрать
        return ! byteBuf.isReadable();


//
//        byteBuf.markReaderIndex();
//
//        byte[] dataEndBytes = new byte[2];
//        byteBuf.readBytes(dataEndBytes);
//
//        byteBuf.resetReaderIndex();
//
//        return Arrays.equals(dataEndBytes, Client.getEndBytes());
    }
}

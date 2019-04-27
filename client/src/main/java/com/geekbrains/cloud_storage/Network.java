package com.geekbrains.cloud_storage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Network {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final byte[] endBytes = new byte[] { (byte) 0, (byte) -1, (byte) 0, (byte) -1 };

    private Selector selector;
    private SocketChannel socketChannel;

    private boolean isSocketReadable = false;
    private boolean isSocketWritable = false;

    private Callback callOnClose;
    private Callback callOnAccept;
    private Callback callOnConnect;
    private Callback callOnRead;
    private Callback callOnWrite;

    {
        Callback empty = args -> { };
        callOnClose = empty;
        callOnAccept = empty;
        callOnConnect = empty;
        callOnRead = empty;
        callOnWrite = empty;
    }

    public void setCallOnClose(Callback callOnClose) {
        this.callOnClose = callOnClose;
    }

    public void setCallOnAccept(Callback callOnAccept) {
        this.callOnAccept = callOnAccept;
    }

    public void setCallOnConnect(Callback callOnConnect) {
        this.callOnConnect = callOnConnect;
    }

    public void setCallOnRead(Callback callOnRead) {
        this.callOnRead = callOnRead;
    }

    public void setCallOnWrite(Callback callOnWrite) {
        this.callOnWrite = callOnWrite;
    }

    public Selector getSelector() {
        return this.selector;
    }

    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    public boolean isSocketReadable() {
        return this.isSocketReadable;
    }

    public boolean isSocketWritable() {
        return this.isSocketWritable;
    }

    public Network(String host, int port) throws IOException {
        this.selector = Selector.open();

        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        this.socketChannel.connect(new InetSocketAddress(host, port));
    }

    public void close() throws IOException {
        this.callOnClose.callback();

        this.selector.close();
        this.socketChannel.finishConnect();
        this.socketChannel.close();
    }

    public void run() throws Exception {
        while (true) {
            int readyChannels = this.selector.selectNow();

            if (readyChannels == 0) {
                continue;
            }

            if (! this.selector.isOpen()) { // нормальный, не форсированный выход при закрытии
                break;
            }

            Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                this.isSocketReadable = key.isReadable();
                this.isSocketWritable = key.isWritable();

                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.

                    this.onAccept();
                } else if (key.isConnectable()) {
                    // a connection was established with a remote server.

                    this.onConnect();
                } else if (key.isReadable()) {
                    // a channel is ready for reading

                    this.onRead();
                } else if (key.isWritable()) {
                    // a channel is ready for writing

                    this.onWrite();
                }

                keyIterator.remove();
            }
        }
    }

    private void onWrite() {
        this.callOnWrite.callback();
    }

    private void onRead() throws IOException {
        this.callOnRead.callback();

        // get meta info
        ///////////////////
        ByteBuffer metaBuffer = ByteBuffer.allocate(256);

        this.socketChannel.read(metaBuffer);

        metaBuffer.flip();

        byte actionType = metaBuffer.get();
        byte optionType = metaBuffer.get();
        short status = metaBuffer.getShort();
        LOGGER.log(Level.INFO, "action: {0}, option: {1}, status: {2}", new Object[] { actionType, optionType, status });

        byte[] metaEndTest = new byte[4];
        metaBuffer.get(metaEndTest);

        if (this.isDataEnd(metaEndTest)) {
            LOGGER.log(Level.INFO, "meta end correct");
        }

        // get data info
        ///////////////////
        ByteBuffer dataBuffer = ByteBuffer.allocate(256);
        dataBuffer.put(metaBuffer); // дочитываем остатки на всякий случай
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ByteBuffer newDataBuffer = null;

        while (true) {
            while (this.socketChannel.read(dataBuffer) > 0) {
                dataBuffer.flip();

                byte[] data = new byte[dataBuffer.remaining()];
                dataBuffer.get(data);
                outputStream.write(data);

                dataBuffer.flip();
            }

            ////////
            newDataBuffer = ByteBuffer.wrap(outputStream.toByteArray());

            byte[] dataEndTest = new byte[4];

            for (int i = 0; i < 4; i++) {
                dataEndTest[i] = newDataBuffer.get(newDataBuffer.capacity() - (4 - i));
            }

            if (this.isDataEnd(dataEndTest)) {
                LOGGER.log(Level.INFO, "data end correct");
                newDataBuffer.limit(newDataBuffer.capacity() - 4);
                break;
            } else {
                LOGGER.log(Level.INFO, "data end NOT correct. re-reading");
            }
        }
        ////////

        ArrayList<String> files = new ArrayList<>();

        while (newDataBuffer.remaining() > 0) {
            int fileNameLength = newDataBuffer.getShort();
            byte[] fileNameBytes = new byte[fileNameLength];

            newDataBuffer.get(fileNameBytes);

            files.add(new String(fileNameBytes));
        }
        ///////////////////

        // print data
        ///////////////////
        for (String file : files) {
            System.out.println(file);
        }
        ///////////////////
    }

    private void onConnect() throws IOException {
        this.callOnConnect.callback();

        this.socketChannel.finishConnect();
    }

    private void onAccept() {
        this.callOnAccept.callback();

        LOGGER.log(Level.INFO, " connection was accepted by a ServerSocketChannel");
    }

    private boolean isDataEnd(byte[] lastBytes) {
        return Arrays.equals(lastBytes, this.endBytes);
    }
}

package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Action.*;
import com.geekbrains.cloud_storage.Contract.OptionType;

import java.awt.image.DataBuffer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Network {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private final int bufferSize = 4;
    private final byte[] endBytes = new byte[] { (byte) 0, (byte) -1 };

    private Selector selector;
    private SocketChannel socketChannel;

    private boolean isSocketReadable = false;
    private boolean isSocketWritable = false;

    private List<NetworkEventListener> onCloseListeners = new LinkedList<>();
    private List<NetworkEventListener> onAcceptListeners = new LinkedList<>();
    private List<NetworkEventListener> onConnectListeners = new LinkedList<>();
    private List<NetworkEventListener> onReadListeners = new LinkedList<>();
    private List<NetworkEventListener> onWriteListeners = new LinkedList<>();

    public void addOnCloseListener(NetworkEventListener eventListener)
    {
        this.onCloseListeners.add(eventListener);
    }

    public void addOnAcceptListener(NetworkEventListener eventListener)
    {
        this.onAcceptListeners.add(eventListener);
    }

    public void addOnConnectListener(NetworkEventListener eventListener)
    {
        this.onConnectListeners.add(eventListener);
    }

    public void addOnReadListener(NetworkEventListener eventListener)
    {
        this.onReadListeners.add(eventListener);
    }

    public void addOnWriteListener(NetworkEventListener eventListener)
    {
        this.onWriteListeners.add(eventListener);
    }

    public byte[] getEndBytes() {
        return this.endBytes;
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

    public void close(SelectionKey key) throws IOException {
        onCloseListeners.forEach(NetworkEventListener::fire);

        key.cancel();
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

                    this.onRead(key);
                } else if (key.isWritable()) {
                    // a channel is ready for writing

                    this.onWrite();
                }

                keyIterator.remove();
            }
        }
    }

    public void writeAndFlush(ByteArrayOutputStream outputStream) throws IOException {
//        outputStream.write(this.endBytes);

        this.socketChannel.write(ByteBuffer.wrap(outputStream.toByteArray()));
    }

//    public void sendMeta(ActionType actionType, OptionType optionType) throws IOException {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        outputStream.writeAndFlush(new byte[] {
//                actionType.getValue(),
//                optionType.getValue(),
//        });
//        outputStream.writeAndFlush(this.endBytes);
//
//        this.socketChannel.writeAndFlush(ByteBuffer.wrap(outputStream.toByteArray()));
//    }

    private void onWrite() {
        this.onWriteListeners.forEach(NetworkEventListener::fire);
    }

    private void onRead(SelectionKey key) throws Exception {
        ByteBuffer dataBuffer = ByteBuffer.allocate(2);

        // =========================================== fill buffer =========================================== \\

        this.socketChannel.read(dataBuffer);

        // ============================================ readResponse data ============================================ \\

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBuffer.array());
        DataInputStream inputStream = new DataInputStream(byteArrayInputStream);

        byte actionTypeByte = inputStream.readByte();
        byte optionTypeByte = inputStream.readByte();
        LOGGER.log(Level.INFO, "action: {0}, option: {1}", new Object[] { actionTypeByte, optionTypeByte });

        ActionType actionType = ActionType.fromByte(actionTypeByte);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);

                switch (accountOptionType) {
                    case SIGN_IN:
                        new AccountSignInResponse(this.socketChannel);
                        break;
                    case SIGN_UP:
                        new AccountSignUpResponse(this.socketChannel);
                        break;
//                    case CHANGE_PASS:
//                    case CHANGE_LOGIN:
//                    case DELETE_ACCOUNT:
//                    default:
//                        new ErrorAction(ctx, actionType, accountOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
//            case DOWNLOAD:
//                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), downloadOptionType} );
//
//                switch (downloadOptionType) {
//                    case FILE:
//                        new DownloadFileAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, downloadOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            case UPLOAD:
//                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), uploadOptionType} );
//
//                switch (uploadOptionType) {
//                    case FILE:
//                        new UploadFileAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, uploadOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            case COMMAND:
//                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), commandOptionType} );
//
//                switch (commandOptionType) {
//                    case FILE_LIST:
//                        new CommandFileListAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, commandOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            default:
//                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN, 404, "UNKNOWN_REQUEST");
//                LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());
        }
    }

    /*private void onRead() throws Exception {
        this.onReadListeners.forEach(NetworkEventListener::fire);

        // get meta info
        ///////////////////
        ByteBuffer metaBuffer = ByteBuffer.allocate(4);

        this.socketChannel.readResponse(metaBuffer);

        metaBuffer.flip();

        byte actionTypeByte = metaBuffer.get();
        byte optionTypeByte = metaBuffer.get();
        short status = metaBuffer.getShort();
        LOGGER.log(Level.INFO, "action: {0}, option: {1}, status: {2}", new Object[] { actionTypeByte, optionTypeByte, status });

//        byte[] metaEndBytes = new byte[2];
//        metaBuffer.get(metaEndBytes);
//
//        if (this.isDataEnd(metaEndBytes)) {
//            LOGGER.log(Level.INFO, "meta end correct");
//        } else {
//            LOGGER.log(Level.INFO, "meta end NOT correct");
//            return;
//        }

*//*        // get data info
        ///////////////////
        ByteBuffer dataBuffer = ByteBuffer.allocate(256);
        dataBuffer.put(metaBuffer); // дочитываем остатки на всякий случай

        this.socketChannel.readResponse(dataBuffer);
        dataBuffer.flip();

        //
        int messageLength = dataBuffer.getInt();

        if (messageLength > 0) {
            byte[] messageBytes = new byte[messageLength];
            dataBuffer.get(messageBytes);

            LOGGER.log(Level.INFO, "message {0}", new String(messageBytes));
        }
        //

        byte[] dataEndBytes = new byte[4];
        dataBuffer.get(dataEndBytes);

        if (this.isDataEnd(dataEndBytes)) {
            LOGGER.log(Level.INFO, "data end correct");
        } else {
            LOGGER.log(Level.INFO, "data end NOT correct");
        }*//*

        // get data info
        ///////////////////
        ActionType actionType = ActionType.fromByte(actionTypeByte);

        switch (actionType) {
            case ACCOUNT:
                AccountOptionType accountOptionType = AccountOptionType.fromByte(optionTypeByte);

                switch (accountOptionType) {
                    case SIGN_IN:
                        new AccountSignInResponse(this.socketChannel, status);
                        break;
                    case SIGN_UP:
                        new AccountSignUpResponse(this.socketChannel, status);
                        break;
//                    case CHANGE_PASS:
//                    case CHANGE_LOGIN:
//                    case DELETE_ACCOUNT:
//                    default:
//                        new ErrorAction(ctx, actionType, accountOptionType, 404, "UNKNOWN_REQUEST");
                }
                break;
//            case DOWNLOAD:
//                DownloadOptionType downloadOptionType = DownloadOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), downloadOptionType} );
//
//                switch (downloadOptionType) {
//                    case FILE:
//                        new DownloadFileAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, downloadOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            case UPLOAD:
//                UploadOptionType uploadOptionType = UploadOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), uploadOptionType} );
//
//                switch (uploadOptionType) {
//                    case FILE:
//                        new UploadFileAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, uploadOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            case COMMAND:
//                CommandOptionType commandOptionType = CommandOptionType.fromByte(optionTypeByte);
//                LOGGER.log(Level.INFO, "{0} -> Option: {1}", new Object[] { ctx.channel().id(), commandOptionType} );
//
//                switch (commandOptionType) {
//                    case FILE_LIST:
//                        new CommandFileListAction(ctx, byteBuf);
//                        break;
//                    default:
//                        new ErrorAction(ctx, actionType, commandOptionType, 404, "UNKNOWN_REQUEST");
//                }
//                break;
//            default:
//                new ErrorAction(ctx, actionType, ErrorOptionType.UNKNOWN, 404, "UNKNOWN_REQUEST");
//                LOGGER.log(Level.WARNING, "{0} -> Err request", ctx.channel().id());
        }

//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        ByteBuffer newDataBuffer = null;
//
//        while (true) {
//            while (this.socketChannel.readResponse(dataBuffer) > 0) {
//                dataBuffer.flip();
//
//                byte[] data = new byte[dataBuffer.remaining()];
//                dataBuffer.get(data);
//                outputStream.writeAndFlush(data);
//
//                dataBuffer.flip();
//            }
//
//            ////////
//            newDataBuffer = ByteBuffer.wrap(outputStream.toByteArray());
//
//            byte[] dataEndTest = new byte[4];
//
//            for (int i = 0; i < 4; i++) {
//                dataEndTest[i] = newDataBuffer.get(newDataBuffer.capacity() - (4 - i));
//            }
//
//            if (this.isDataEnd(dataEndTest)) {
//                LOGGER.log(Level.INFO, "data end correct");
//                newDataBuffer.limit(newDataBuffer.capacity() - 4);
//                break;
//            } else {
//                LOGGER.log(Level.INFO, "data end NOT correct. re-reading");
//            }
//        }
//        ////////
//
//        ArrayList<String> files = new ArrayList<>();
//
//        while (newDataBuffer.remaining() > 0) {
//            int fileNameLength = newDataBuffer.getShort();
//            byte[] fileNameBytes = new byte[fileNameLength];
//
//            newDataBuffer.get(fileNameBytes);
//
//            files.add(new String(fileNameBytes));
//        }
//        ///////////////////
//
//        // print data
//        ///////////////////
//        for (String file : files) {
//            System.out.println(file);
//        }
//        ///////////////////
    }*/

    private void onConnect() throws IOException {
        this.onConnectListeners.forEach(NetworkEventListener::fire);
        this.socketChannel.finishConnect();

        LOGGER.log(Level.INFO, "Connected to a server");
    }

    private void onAccept() {
        this.onAcceptListeners.forEach(NetworkEventListener::fire);

        LOGGER.log(Level.INFO, "Connection was accepted by a ServerSocketChannel");
    }

    private boolean isDataEnd(byte[] lastBytes) {
        return Arrays.equals(lastBytes, this.endBytes);
    }
}

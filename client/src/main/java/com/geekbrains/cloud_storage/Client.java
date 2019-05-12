package com.geekbrains.cloud_storage;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Application {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    //    private static Network network;
    private static Channel network;
    private static GUI gui = new GUI();
    private static Auth auth = new Auth();

    private String host = "localhost";
    private int port = 8189;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    //    public static Network getNetwork() {
//        return network;
//    }
    public static Channel getNetwork() {
        return network;
    }

    public static byte[] getEndBytes() {
        return new byte[] { (byte) 0, (byte) -1 };
    }

    public static GUI getGui() {
        return gui;
    }

    public static Auth getAuth() {
        return auth;
    }

    @Override
    public void init() throws InterruptedException {
        // init logger
        Common.initLogger(LOGGER);

        // init networking
        this.initNetwork();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui.initMainWindow(primaryStage);

        if (auth.isSignedIn()) {
            gui.getMainStage().show();
        } else {
            gui.initSignInScene();
            gui.getSignInStage().show();
        }
    }

    /*
     * TODO KeepAlive packet
     * TODO Append auth key in restricted area
     * TODO Save and restore Auth key using conf file
     * */
    private void initNetwork() {
        new Thread(() -> {
            while (true) {
                EventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    Bootstrap bootstrap = new Bootstrap(); // (1)
                    bootstrap.group(workerGroup); // (2)
                    bootstrap.channel(NioSocketChannel.class); // (3)
                    bootstrap.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                    bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(8196, Unpooled.wrappedBuffer(Client.getEndBytes())));
                            socketChannel.pipeline().addLast(new InClientHandler(), new OutClientHandler());
                        }
                    });

                    // Start the client.
                    ChannelFuture channelFuture = bootstrap.connect(this.host, this.port).sync(); // (5)

                    network = channelFuture.channel();

                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Disconnected. Re-Connecting...");
                    e.printStackTrace();
                } finally {
                    workerGroup.shutdownGracefully();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

//        new Thread(() -> {
//            while (true) {
//                try {
//                    Client.network = new Network(this.host, this.port);
//                    Client.network.run();
//                } catch (ConnectException ignored) {
//                    LOGGER.log(Level.WARNING, "Disconnected. Re-Connecting...");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    break;
//                }
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }
}
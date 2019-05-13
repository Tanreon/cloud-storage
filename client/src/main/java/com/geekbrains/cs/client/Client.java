package com.geekbrains.cs.client;

import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.client.Handler.InClientHandler;
import com.geekbrains.cs.client.Handler.OutClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Application {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

    private static Channel networkChannel;
    private static GUI gui = new GUI();
    private static Auth auth = new Auth();

    private String host = "localhost";
    private int port = 8189;

    public static byte[] getEndBytes() { // FIXME убрать отсюда. тут этому не место
        return new byte[] { 0, -1, 1, -1, 0 };
    }

    public static Channel getNetworkChannel() {
        return networkChannel;
    }

    public static GUI getGui() {
        return gui;
    }

    public static Auth getAuth() {
        return auth;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {
        // init logger
        Common.initLogger(LOGGER, Level.INFO);

        // init networking
        this.initNetwork();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Client.gui.addOnCloseListener(() -> {
            if (Client.networkChannel.isOpen()) {
                Client.networkChannel.close();
            }

            System.exit(0);
        });

        Client.gui.initMainWindow(primaryStage);

        if (Client.auth.isSignedIn()) {
            Client.gui.getMainStage().show();
        } else {
            Client.gui.initSignInScene();
            Client.gui.getSignInStage().show();
        }
    }

    /*
     * TODO KeepAlive packet
     * TODO Append auth key in restricted area
     * TODO Save and restore Auth key using conf file
     * */
    private void initNetwork() {
        Thread thread = new Thread(() -> {
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
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(65 * 1024, Unpooled.wrappedBuffer(Client.getEndBytes())));
                            socketChannel.pipeline().addLast(new InClientHandler(), new OutClientHandler());
                        }
                    });

                    // Start the client.
                    ChannelFuture channelFuture = bootstrap.connect(this.host, this.port).sync(); // (5)

                    Client.networkChannel = channelFuture.channel();

                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Disconnected. Re-Connecting...");
                } finally {
                    workerGroup.shutdownGracefully();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }
}
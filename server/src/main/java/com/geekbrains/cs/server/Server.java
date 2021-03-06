package com.geekbrains.cs.server;

import com.geekbrains.cs.common.Common;
import com.geekbrains.cs.server.Handlers.InHandler;
import com.geekbrains.cs.server.Handlers.InMiddlewareHandler;
import com.geekbrains.cs.server.Handlers.OutHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    public static final String STORAGE_PATH = "server_storage";

    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static SQLHandler sqlHandler;

    private int port;

    public static ExecutorService getExecutor() {
        return executor;
    }

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        // init methods before start server
        Common.initLogger(LOGGER, Level.INFO);

        // start server
        new Server(8189).run();
    }

    public void run() throws Exception {
        sqlHandler = new SQLHandler();

        EventLoopGroup bossGroup   = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            sqlHandler.connect();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 128);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
//            bootstrap.childOption(ChannelOption.TCP_NODELAY, true); // отключено намерено так как много маленьких пакетиков
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    channel.pipeline().addLast(new DelimiterBasedFrameDecoder(Common.MAX_BUFFER_LENGTH, Unpooled.wrappedBuffer(Common.END_BYTES)));
                    channel.pipeline().addLast(new OutHandler());
                    channel.pipeline().addLast(new InMiddlewareHandler());
                    channel.pipeline().addLast(new InHandler());
                }
            });

            // Bind and start to accept incoming connections.
            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            sqlHandler.disconnect();
        }
    }

    public static SQLHandler getSqlHandler() {
        return sqlHandler;
    }
}

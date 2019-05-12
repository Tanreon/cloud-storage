package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Handler.InServerHandler;
import com.geekbrains.cloud_storage.Handler.OutServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

import java.util.logging.Logger;

public class Server {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static SQLHandler sqlHandler;

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        // init methods before start server
        Common.initLogger(LOGGER);

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
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
//            bootstrap.childOption(ChannelOption.TCP_NODELAY, true); // отключено намерено так как много маленьких пакетиков
            bootstrap.childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel channel) {
                    channel.pipeline().addLast(new DelimiterBasedFrameDecoder(8196, Unpooled.wrappedBuffer(new byte[] { (byte) 0, (byte) -1 })));
                    channel.pipeline().addLast(new OutServerHandler(), new InServerHandler());
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

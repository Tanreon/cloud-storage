package com.geekbrains.cs.server.Middlewares;

import com.geekbrains.cs.common.Contracts.EmptyHeaderException;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.server.Handlers.InMiddlewareHandler;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.server.Server;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMiddleware extends AbstractMiddleware {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public MainMiddleware(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;
    }

    public void init() {
        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyHeaderException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyHeaderException", this.ctx.channel().id());
            this.writeMiddlewareAndFlush(new MiddlewareResponse(HeaderType.UNKNOWN, "BAD_HEADER"));
        }
    }

    @Override
    protected void receiveDataByProtocol() throws EmptyHeaderException {
        if (! this.inByteBuf.isReadable()) {
            throw new EmptyHeaderException();
        }

        { // read all headers
            this.headersMap = this.readHeaders();
        }
    }

    @Override
    protected void process() {
        this.ctx.pipeline().get(InMiddlewareHandler.class).setHeadersMap(this.headersMap);

        // next middleware
        this.next();
    }

    private LinkedHashMap<HeaderType, String> readHeaders() {
        byte headersCount = this.inByteBuf.readByte();

        LinkedHashMap<HeaderType, String> headersMap = new LinkedHashMap<>();

        for (int i = 0; i < headersCount; i++) {
            byte headerTypeByte = this.inByteBuf.readByte();
            String header = this.readStringByShort();

            headersMap.put(HeaderType.fromByte(headerTypeByte), header);
        }

        return headersMap;
    }
}

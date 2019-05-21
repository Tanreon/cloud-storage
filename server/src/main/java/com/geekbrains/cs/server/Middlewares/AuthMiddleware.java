package com.geekbrains.cs.server.Middlewares;

import com.geekbrains.cs.common.Contracts.EmptyHeaderException;
import com.geekbrains.cs.common.Contracts.ProcessException;
import com.geekbrains.cs.server.Events.AuthMiddlewareEvent;
import com.geekbrains.cs.server.Contracts.EmptyResultException;
import com.geekbrains.cs.server.Contracts.InvalidHeaderException;
import com.geekbrains.cs.common.HeaderType;
import com.geekbrains.cs.server.Contracts.ProcessFailureException;
import com.geekbrains.cs.server.MiddlewareResponse;
import com.geekbrains.cs.server.Server;
import com.geekbrains.cs.server.Services.AccountSignService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthMiddleware extends AbstractMiddleware {
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private final HeaderType HEADER_TYPE = HeaderType.AUTH;

    private String key = null;

    public AuthMiddleware(ChannelHandlerContext ctx, ByteBuf byteBuf) {
        this.ctx = ctx;
        this.inByteBuf = byteBuf;

        try {
            // Run protocol request processing
            this.receiveDataByProtocol();
            // Run data processing
            this.process();
        } catch (EmptyHeaderException ex) {
            LOGGER.log(Level.INFO, "{0} -> EmptyHeaderException", ctx.channel().id());
            this.writeMiddlewareAndFlush(new MiddlewareResponse(HEADER_TYPE, "BAD_HEADER"));
        } catch (InvalidHeaderException ex) {
            LOGGER.log(Level.INFO, "{0} -> InvalidHeaderException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            this.writeMiddlewareAndFlush(new MiddlewareResponse(HEADER_TYPE, ex.getMessage()));
        } catch (ProcessException ex) {
            LOGGER.log(Level.INFO, "{0} -> ProcessException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            this.writeMiddlewareAndFlush(new MiddlewareResponse(HEADER_TYPE, ex.getMessage()));
        } catch (ProcessFailureException ex) {
            LOGGER.log(Level.WARNING, "{0} -> ProcessFailureException: {1}", new Object[] { ctx.channel().id(), ex.getMessage() });
            this.writeMiddlewareAndFlush((MiddlewareResponse) ex.getResponse());
        }
    }

    @Override
    protected void receiveDataByProtocol() throws EmptyHeaderException, InvalidHeaderException {
        if (! this.inByteBuf.isReadable()) {
            throw new EmptyHeaderException();
        }

        {
            this.headersMap = this.readHeaders();
        }

        if (! this.headersMap.containsKey(HEADER_TYPE)) {
            throw new InvalidHeaderException("AUTH_HEADER_NOT_FOUND");
        }

        { // read key
            this.key = this.headersMap.get(HEADER_TYPE);
        }
    }

    @Override
    protected void process() throws ProcessException, ProcessFailureException {
        if (! AccountSignService.isKeyCorrect(this.key)) {
            throw new ProcessException("KEY_NOT_CORRECT");
        }

        String login;

        try {
            login = AccountSignService.getLoginByKey(this.key);
        } catch (EmptyResultException ex) {
            throw new ProcessFailureException(new MiddlewareResponse(HEADER_TYPE, "SERVER_ERROR"), ex.getMessage());
        }

        {
            AuthMiddlewareEvent authEvent = new AuthMiddlewareEvent();
            authEvent.setSignedIn(true);
            authEvent.setLogin(login);

            this.ctx.fireUserEventTriggered(authEvent);
        }
    }
}

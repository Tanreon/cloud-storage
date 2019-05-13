package com.geekbrains.cs.server.Action;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;
import com.geekbrains.cs.common.Response.BaseAbstractResponse;
import com.geekbrains.cs.server.Response;
import com.geekbrains.cs.server.Server;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractAction extends BaseAbstractResponse {
    protected static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    protected abstract boolean sendDataByProtocol() throws Exception;

    protected void rejectEmpty(ActionType actionType, OptionType optionType) {
        LOGGER.log(Level.WARNING, "{0} -> Protocol receiving err", ctx.channel().id());
        ctx.writeAndFlush(new Response(actionType, optionType, 400, "BAD_REQUEST"));
    }
}

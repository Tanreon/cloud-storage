package com.geekbrains.cs.server.Actions;

import com.geekbrains.cs.common.Responses.BaseAbstractResponse;

public abstract class AbstractAction extends BaseAbstractResponse {
    protected abstract void sendDataByProtocol() throws Exception;
}

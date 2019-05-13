package com.geekbrains.cs.client.Request;

import com.geekbrains.cs.common.BaseAbstractNetworkInteraction;

abstract public class AbstractRequest extends BaseAbstractNetworkInteraction {
    protected abstract boolean sendDataByProtocol() throws Exception;
}
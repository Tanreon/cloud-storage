package com.geekbrains.cs.server;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;

public class AbstractResponse {
    protected ActionType actionType;
    protected OptionType optionType;
    protected int status;
    protected boolean last;
    protected String message;

    public boolean isLast() {
        return this.last;
    }

    public boolean hasMessage() {
        return this.message.length() > 0;
    }

    public String getMessage() {
        return message;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public int getStatus() {
        return status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.geekbrains.cs.server;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contract.OptionType;

public class Response {
    private ActionType actionType;
    private OptionType optionType;
    private int status;
    private String message;
    private boolean last;

    public Response(ActionType actionType, OptionType optionType, int status) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.last = true;
    }

    public Response(ActionType actionType, OptionType optionType, int status, boolean last) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.last = last;
    }

    public Response(ActionType actionType, OptionType optionType, int status, String message) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;
        this.last = true;
    }

    public Response(ActionType actionType, OptionType optionType, int status, String message, boolean last) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;
        this.last = last;
    }

    public boolean last() {
        return this.last;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public boolean hasMessage() {
        return this.message.length() > 0;
    }
}

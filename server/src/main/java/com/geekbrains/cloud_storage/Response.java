package com.geekbrains.cloud_storage;

import com.geekbrains.cloud_storage.Contract.OptionType;

public class Response {
    private ActionType actionType;
    private OptionType optionType;
    private int status;
    private String message;

    public Response(ActionType actionType, OptionType optionType, int status) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
    }

    public Response(ActionType actionType, OptionType optionType, int status, String message) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;
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

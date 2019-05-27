package com.geekbrains.cs.server;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;

public class ActionResponse extends AbstractResponse {
    protected ActionType actionType;
    protected OptionType optionType;
    protected int status;

    public ActionType getActionType() {
        return actionType;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public int getStatus() {
        return status;
    }

    public ActionResponse(ActionType actionType, OptionType optionType, int status) {
        this(actionType, optionType, status, true);
    }

    public ActionResponse(ActionType actionType, OptionType optionType, int status, boolean last) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.last = last;
    }

    public ActionResponse(ActionType actionType, OptionType optionType, int status, String message) {
        this(actionType, optionType, status, message, true);
    }

    public ActionResponse(ActionType actionType, OptionType optionType, int status, String message, boolean last) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.status = status;
        this.message = message;
        this.last = last;
    }
}

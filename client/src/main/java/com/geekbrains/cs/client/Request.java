package com.geekbrains.cs.client;

import com.geekbrains.cs.common.ActionType;
import com.geekbrains.cs.common.Contracts.OptionType;

public class Request {
    private ActionType actionType;
    private OptionType optionType;
    private boolean last;

    public Request(ActionType actionType, OptionType optionType) {
        this(actionType, optionType, true);
    }

    public Request(ActionType actionType, OptionType optionType, boolean last) {
        this.actionType = actionType;
        this.optionType = optionType;
        this.last = last;
    }

    public boolean isLast() {
        return this.last;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public OptionType getOptionType() {
        return optionType;
    }
}

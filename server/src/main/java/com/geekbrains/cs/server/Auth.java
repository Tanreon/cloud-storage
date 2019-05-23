package com.geekbrains.cs.server;

import com.geekbrains.cs.server.Contracts.MiddlewareEvent;

public class Auth implements MiddlewareEvent {
    private String login;

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

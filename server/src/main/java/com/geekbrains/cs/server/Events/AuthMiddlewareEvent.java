package com.geekbrains.cs.server.Events;

import com.geekbrains.cs.server.Contracts.MiddlewareEvent;

public class AuthMiddlewareEvent implements MiddlewareEvent {
    private boolean signedIn = false;
    private String login;

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isSignedIn() {
        return this.signedIn;
    }

    public void setSignedIn(boolean signedIn) {
        this.signedIn = signedIn;
    }
}

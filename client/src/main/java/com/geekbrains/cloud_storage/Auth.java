package com.geekbrains.cloud_storage;

public class Auth {
    private String key;
    private String login;

    public void setKey(String key) {
        this.key = key;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return this.login;
    }

    public String getKey() {
        return key;
    }

    public boolean isSignedIn() {
        return this.key == null;
    }
}

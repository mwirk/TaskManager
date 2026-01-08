package com.taskmanager.org.dto;

public class LoginResponse {
    private String token;
    private long expiresIn;
    private String error;

    public LoginResponse setToken(String token) {
        this.token = token;
        return this;
    }

    public LoginResponse setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }


    public String getToken() { return token; }
    public long getExpiresIn() { return expiresIn; }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

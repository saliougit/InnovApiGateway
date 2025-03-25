package com.innov4africa.api_gateway.model;

public class AuthResult {
    private boolean success;
    private String message;
    private String token;

    // Constructeur pour le cas sans token
    public AuthResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Constructeur avec token
    public AuthResult(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
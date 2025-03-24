package com.innov4africa.api_gateway.model;

public class AuthResult {
    private boolean success;
    private String message;

    // Constructeur
    public AuthResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // Setters (optionnels)
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
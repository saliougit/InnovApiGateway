
package com.innov4africa.api_gateway.model;

public class AuthResult {
    private boolean success;
    private String message;
    private String token;
    private String nom;
    private String prenom;

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

    // Nouveau constructeur avec tous les champs
    public AuthResult(boolean success, String message, String token, String nom, String prenom) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters et Setters pour tous les champs
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
package com.innov4africa.api_gateway.model;

/**
 * Classe représentant une transaction/opération sur un compte
 */
public class Transaction {
    private String date;
    private String montant;
    private String type;
    private String description;
    private String reference;
    
    public Transaction() {
    }
    
    public Transaction(String date, String montant, String type, String description, String reference) {
        this.date = date;
        this.montant = montant;
        this.type = type;
        this.description = description;
        this.reference = reference;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
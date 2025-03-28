package com.innov4africa.api_gateway.model;

public class HistoryItem {
    private String date;
    private String montant;
    private String operation;

    // Constructeurs
    public HistoryItem() {}

    public HistoryItem(String date, String montant, String operation) {
        this.date = date;
        this.montant = montant;
        this.operation = operation;
    }

    // Getters et Setters
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

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "HistoryItem{" +
                "date='" + date + '\'' +
                ", montant='" + montant + '\'' +
                ", operation='" + operation + '\'' +
                '}';
    }
}
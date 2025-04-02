package com.innov4africa.api_gateway.model;

/**
 * Classe représentant une requête de paiement iShop
 */
public class IShopPaymentRequest {
    private String numeros;
    private String montant;
    private String order;
    private String code;

    public IShopPaymentRequest() {
    }

    public IShopPaymentRequest(String numeros, String montant, String order, String code) {
        this.numeros = numeros;
        this.montant = montant;
        this.order = order;
        this.code = code;
    }

    public String getNumeros() {
        return numeros;
    }

    public void setNumeros(String numeros) {
        this.numeros = numeros;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
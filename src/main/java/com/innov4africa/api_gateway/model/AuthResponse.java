package com.innov4africa.api_gateway.model;

import java.util.List;


public class AuthResponse{
     private String status;
    private String message;
    private String token;
    private List<ServiceStatus> serviceStatuses;

    // Constructeurs, getters, setters
    public AuthResponse() {}

    public AuthResponse(String status, String message, String token, List<ServiceStatus> serviceStatuses) {
        this.status = status;
        this.message = message;
        this.token = token;
        this.serviceStatuses = serviceStatuses;
    }

    // Getters et setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public List<ServiceStatus> getServiceStatuses() { return serviceStatuses; }
    public void setServiceStatuses(List<ServiceStatus> serviceStatuses) { this.serviceStatuses = serviceStatuses; }
}
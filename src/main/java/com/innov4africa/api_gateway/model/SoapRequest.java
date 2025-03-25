package com.innov4africa.api_gateway.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"header", "body"})
public class SoapRequest {
    
    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = false)
    private String header;
    
    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = true)
    private SoapBody body;
    
    public SoapRequest() {
    }
    
    public SoapRequest(LoginRequest loginRequest) {
        this.body = new SoapBody(loginRequest);
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Body", propOrder = {"login"})
    public static class SoapBody {
        
        @XmlElement(name = "login", namespace = "http://runtime.services.cash.innov.sn/")
        private LoginRequest login;
        
        public SoapBody() {
        }
        
        public SoapBody(LoginRequest loginRequest) {
            this.login = loginRequest;
        }
        
        public LoginRequest getLogin() {
            return login;
        }
        
        public void setLogin(LoginRequest login) {
            this.login = login;
        }
    }
    
    public String getHeader() {
        return header;
    }
    
    public void setHeader(String header) {
        this.header = header;
    }
    
    public SoapBody getBody() {
        return body;
    }
    
    public void setBody(SoapBody body) {
        this.body = body;
    }
}
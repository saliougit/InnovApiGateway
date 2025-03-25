package com.innov4africa.api_gateway.model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"header", "body"})
public class SoapResponse {
    
    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = false)
    private String header;
    
    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/", required = true)
    private SoapBody body;
    
    public SoapResponse() {
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "Body", propOrder = {"loginResponse"})
    public static class SoapBody {
        
        @XmlElement(name = "loginResponse", namespace = "http://runtime.services.cash.innov.sn/")
        private LoginResponse loginResponse;
        
        public SoapBody() {
        }
        
        public LoginResponse getLoginResponse() {
            return loginResponse;
        }
        
        public void setLoginResponse(LoginResponse loginResponse) {
            this.loginResponse = loginResponse;
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
    
    public LoginResponse getLoginResponse() {
        return body != null ? body.getLoginResponse() : null;
    }
}
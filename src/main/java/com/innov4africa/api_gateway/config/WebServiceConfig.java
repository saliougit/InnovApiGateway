package com.innov4africa.api_gateway.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapMessage;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class WebServiceConfig {

    @Bean
    public SaajSoapMessageFactory messageFactory() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory(messageFactory);
        saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_11);
        saajSoapMessageFactory.afterPropertiesSet();
        return saajSoapMessageFactory;
    }

    @Bean   
    public CloseableHttpClient httpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        cm.setDefaultMaxPerRoute(20);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    @Bean
    public HttpComponentsMessageSender messageSender(CloseableHttpClient httpClient) {
        HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender(httpClient);
        messageSender.setConnectionTimeout(30000);
        messageSender.setReadTimeout(30000);
        return messageSender;
    }

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        
        marshaller.setClassesToBeBound(
            com.innov4africa.api_gateway.model.LoginRequest.class,
            com.innov4africa.api_gateway.model.LoginResponse.class,
            com.innov4africa.api_gateway.model.SoapRequest.class,
            com.innov4africa.api_gateway.model.SoapRequest.SoapBody.class,
            com.innov4africa.api_gateway.model.SoapResponse.class,
            com.innov4africa.api_gateway.model.SoapResponse.SoapBody.class
        );

        marshaller.setPackagesToScan("com.innov4africa.api_gateway.model");
        
        Map<String, Object> props = new HashMap<>();
        props.put(jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        props.put(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setMarshallerProperties(props);

        return marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate(SaajSoapMessageFactory messageFactory, 
                                               HttpComponentsMessageSender messageSender,
                                               Jaxb2Marshaller marshaller) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
        webServiceTemplate.setMessageSender(messageSender);
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri("https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS");
        
        WebServiceMessageCallback soapHeaderCallback = new WebServiceMessageCallback() {
            @Override
            public void doWithMessage(WebServiceMessage message) {
                SoapMessage soapMessage = (SoapMessage) message;
                soapMessage.getSoapHeader().addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
                soapMessage.getSoapHeader().addNamespaceDeclaration("run", "http://runtime.services.cash.innov.sn/");
            }
        };
        
        webServiceTemplate.setDefaultCallback(soapHeaderCallback);
        
        return webServiceTemplate;
    }
}
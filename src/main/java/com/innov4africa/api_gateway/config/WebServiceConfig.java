package com.innov4africa.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;

import java.time.Duration;
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
    public HttpUrlConnectionMessageSender messageSender() {
        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(Duration.ofSeconds(30));
        sender.setReadTimeout(Duration.ofSeconds(30));
        return sender;
    }

    @Bean
    public ClientInterceptor soapHeaderInterceptor() {
        return new ClientInterceptorAdapter() {
            @Override
            public boolean handleRequest(MessageContext messageContext) {
                try {
                    SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
                    soapMessage.getSoapHeader().addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
                    soapMessage.getSoapHeader().addNamespaceDeclaration("run", "http://runtime.services.cash.innov.sn/");
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        };
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
    public WebServiceTemplate webServiceTemplate(
        SaajSoapMessageFactory messageFactory, 
        HttpUrlConnectionMessageSender messageSender,
        Jaxb2Marshaller marshaller,
        ClientInterceptor soapHeaderInterceptor) {
        
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
        webServiceTemplate.setMessageSender(messageSender);
        webServiceTemplate.setMarshaller(marshaller);
        webServiceTemplate.setUnmarshaller(marshaller);
        webServiceTemplate.setDefaultUri("https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS");
        webServiceTemplate.setInterceptors(new ClientInterceptor[]{soapHeaderInterceptor});
        
        return webServiceTemplate;
    }
}
// package com.innov4africa.api_gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import com.innov4africa.api_gateway.model.LoginRequest; // Import the LoginRequest class
// import com.innov4africa.api_gateway.model.LoginResponse;
// import com.innov4africa.api_gateway.model.SoapRequest;
// import com.innov4africa.api_gateway.model.SoapResponse;

// import org.springframework.oxm.jaxb.Jaxb2Marshaller;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.client.support.interceptor.ClientInterceptor;
// import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
// import org.springframework.ws.context.MessageContext;
// import org.springframework.ws.soap.SoapMessage;
// import org.springframework.ws.soap.SoapVersion;
// import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
// import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

// import jakarta.xml.soap.MessageFactory;
// import jakarta.xml.soap.SOAPConstants;
// import jakarta.xml.soap.SOAPException;

// import java.io.IOException;
// import java.time.Duration;

// @Configuration
// public class WebServiceConfig {

//     @Bean
//     public SaajSoapMessageFactory messageFactory() throws SOAPException {
//         MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
//         SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory(messageFactory);
//         saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_11);
//         return saajSoapMessageFactory;
//     }

//     @Bean
//     public HttpUrlConnectionMessageSender messageSender() {
//         HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
//         sender.setConnectionTimeout(Duration.ofSeconds(30));
//         sender.setReadTimeout(Duration.ofSeconds(30));
//         return sender;
//     }

//     // @Bean
//     // public ClientInterceptor soapHeaderInterceptor() {
//     //     return new ClientInterceptorAdapter() {
//     //         @Override
//     //         public boolean handleRequest(MessageContext messageContext) {
//     //             try {
//     //                 SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
//     //                 soapMessage.getSoapHeader().addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
//     //                 soapMessage.getSoapHeader().addNamespaceDeclaration("run", "http://runtime.services.cash.innov.sn/");
//     //                 return true;
//     //             } catch (Exception e) {
//     //                 return false;
//     //             }
//     //         }
//     //     };
//     // }

//     @Bean
//     public Jaxb2Marshaller marshaller() {
//         Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//         // **IMPORTANT:** Spécifie toutes les classes à mapper. Ceci est plus robuste.
//         marshaller.setClassesToBeBound(
//                 LoginRequest.class,
//                 LoginResponse.class,
//                 SoapRequest.class,
//                 SoapResponse.class
//         );
//         return marshaller;
//     }

//     @Bean
//     public WebServiceTemplate webServiceTemplate(
//             SaajSoapMessageFactory messageFactory,
//             HttpUrlConnectionMessageSender messageSender,
//             Jaxb2Marshaller marshaller,
//             ClientInterceptor soapHeaderInterceptor) {

//         WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
//         webServiceTemplate.setMessageSender(messageSender);
//         webServiceTemplate.setMarshaller(marshaller);
//         webServiceTemplate.setUnmarshaller(marshaller);
//         webServiceTemplate.setDefaultUri("https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS");
//         webServiceTemplate.setInterceptors(new ClientInterceptor[]{soapHeaderInterceptor});

//         return webServiceTemplate;
//     }
// }


// the last step is to add the following code to the WebServiceConfig.java file

// package com.innov4africa.api_gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import com.innov4africa.api_gateway.model.LoginRequest;
// import com.innov4africa.api_gateway.model.LoginResponse;
// import com.innov4africa.api_gateway.model.SoapRequest;
// import com.innov4africa.api_gateway.model.SoapResponse;
// import org.springframework.oxm.jaxb.Jaxb2Marshaller;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.soap.SoapVersion;
// import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
// import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
// import jakarta.xml.soap.MessageFactory;
// import jakarta.xml.soap.SOAPConstants;
// import jakarta.xml.soap.SOAPException;
// import java.io.IOException;
// import java.time.Duration;

// @Configuration
// public class WebServiceConfig {

//     @Bean
//     public SaajSoapMessageFactory messageFactory() throws SOAPException {
//         MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
//         SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory(messageFactory);
//         saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_11);
//         return saajSoapMessageFactory;
//     }

//     @Bean
//     public HttpUrlConnectionMessageSender messageSender() {
//         HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
//         sender.setConnectionTimeout(Duration.ofSeconds(30));
//         sender.setReadTimeout(Duration.ofSeconds(30));
//         return sender;
//     }

//     @Bean
//     public Jaxb2Marshaller marshaller() {
//         Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//         // **IMPORTANT:** Spécifiez toutes les classes à mapper. Ceci est plus robuste.
//         marshaller.setClassesToBeBound(
//                 LoginRequest.class,
//                 LoginResponse.class,
//                 SoapRequest.class,
//                 SoapResponse.class
//         );
//         return marshaller;
//     }

//     @Bean
//     public WebServiceTemplate webServiceTemplate(
//             SaajSoapMessageFactory messageFactory,
//             HttpUrlConnectionMessageSender messageSender,
//             Jaxb2Marshaller marshaller) throws IOException {

//         WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
//         webServiceTemplate.setMessageSender(messageSender);
//         webServiceTemplate.setMarshaller(marshaller);
//         webServiceTemplate.setUnmarshaller(marshaller);
//         webServiceTemplate.setDefaultUri("https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS");
//         return webServiceTemplate;
//     }
// }


// package com.innov4africa.api_gateway.config;

// import org.apache.http.impl.client.HttpClients;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.transport.http.HttpComponentsMessageSender;

// @Configuration
// public class WebServiceConfig {

//     @Bean
//     public WebServiceTemplate webServiceTemplate() {
//         WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
//         HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender();
//         messageSender.setHttpClient(HttpClients.createDefault());
//         webServiceTemplate.setMessageSender(messageSender);
//         messageSender.setConnectionTimeout(5000);
//         messageSender.setReadTimeout(10000);
//         return webServiceTemplate;
//     }
// }


package com.innov4africa.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}


// package com.innov4africa.api_gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.soap.SoapVersion;
// import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
// import org.springframework.oxm.jaxb.Jaxb2Marshaller;
// import org.springframework.ws.client.support.interceptor.ClientInterceptor;
// import org.springframework.ws.context.MessageContext;
// import org.springframework.ws.soap.SoapMessage;
// import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
// import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

// import jakarta.xml.soap.MessageFactory;
// import jakarta.xml.soap.SOAPConstants;
// import jakarta.xml.soap.SOAPException;

// import java.time.Duration;
// import java.util.HashMap;
// import java.util.Map;

// @Configuration
// public class WebServiceConfig {

//     @Bean
//     public SaajSoapMessageFactory messageFactory() throws SOAPException {
//         MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
//         SaajSoapMessageFactory saajSoapMessageFactory = new SaajSoapMessageFactory(messageFactory);
//         saajSoapMessageFactory.setSoapVersion(SoapVersion.SOAP_11);
//         saajSoapMessageFactory.afterPropertiesSet();
//         return saajSoapMessageFactory;
//     }

//     @Bean
//     public HttpUrlConnectionMessageSender messageSender() {
//         HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
//         sender.setConnectionTimeout(Duration.ofSeconds(30));
//         sender.setReadTimeout(Duration.ofSeconds(30));
//         return sender;
//     }

//     @Bean
//     public ClientInterceptor soapHeaderInterceptor() {
//         return new ClientInterceptorAdapter() {
//             @Override
//             public boolean handleRequest(MessageContext messageContext) {
//                 try {
//                     SoapMessage soapMessage = (SoapMessage) messageContext.getRequest();
//                     soapMessage.getSoapHeader().addNamespaceDeclaration("soap", "http://schemas.xmlsoap.org/soap/envelope/");
//                     soapMessage.getSoapHeader().addNamespaceDeclaration("run", "http://runtime.services.cash.innov.sn/");
//                     return true;
//                 } catch (Exception e) {
//                     return false;
//                 }
//             }
//         };
//     }

//     @Bean
//     public Jaxb2Marshaller marshaller() {
//         Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//         marshaller.setContextPath("com.innov4africa.api_gateway.model");
//         return marshaller;
//     }

//     @Bean
//     public WebServiceTemplate webServiceTemplate(
//         SaajSoapMessageFactory messageFactory, 
//         HttpUrlConnectionMessageSender messageSender,
//         Jaxb2Marshaller marshaller,
//         ClientInterceptor soapHeaderInterceptor) {
        
//         WebServiceTemplate webServiceTemplate = new WebServiceTemplate(messageFactory);
//         webServiceTemplate.setMessageSender(messageSender);
//         webServiceTemplate.setMarshaller(marshaller);
//         webServiceTemplate.setUnmarshaller(marshaller);
//         webServiceTemplate.setDefaultUri("https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS");
//         webServiceTemplate.setInterceptors(new ClientInterceptor[]{soapHeaderInterceptor});
        
//         return webServiceTemplate;
//     }
// }
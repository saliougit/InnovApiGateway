package com.innov4africa.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class WebServiceConfig {

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        // Pas besoin de marshaller/unmarshaller pour des chaînes XML brutes
        return webServiceTemplate;
    }
}


// package com.innov4africa.api_gateway.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.oxm.jaxb.Jaxb2Marshaller;
// import org.springframework.ws.client.core.WebServiceTemplate;

// @Configuration
// public class WebServiceConfig {

//     @Bean
//     public WebServiceTemplate webServiceTemplate() {
//         WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
//         webServiceTemplate.setMarshaller(marshaller());
//         webServiceTemplate.setUnmarshaller(marshaller());
//         return webServiceTemplate;
//     }

//     @Bean
//     public Jaxb2Marshaller marshaller() {
//         Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
//         marshaller.setContextPath("com.innov4africa.api_gateway.wsdl"); // Package pour les classes générées par JAXB (si nécessaire)
//         return marshaller;
//     }
// }
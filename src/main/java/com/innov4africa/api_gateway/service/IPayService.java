// package com.innov4africa.api_gateway.service;

// import com.innov4africa.api_gateway.model.AuthResult;
// import org.springframework.stereotype.Service;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.soap.client.core.SoapActionCallback;
// import reactor.core.publisher.Mono;

// import javax.xml.transform.stream.StreamResult;
// import javax.xml.transform.stream.StreamSource;
// import java.io.StringReader;
// import java.io.StringWriter;

// @Service
// public class IPayService {

//     private final WebServiceTemplate webServiceTemplate;

//     public IPayService(WebServiceTemplate webServiceTemplate) {
//         this.webServiceTemplate = webServiceTemplate;
//     }

//     public Mono<AuthResult> authenticate(String email, String password) {

//         // Désactiver la vérification SSL
//         SSLUtil.disableSslVerification();
        
//         // Construire la requête SOAP
//         String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
//                 "   <soapenv:Header/>\n" +
//                 "   <soapenv:Body>\n" +
//                 "      <run:login>\n" +
//                 "         <login>" + email + "</login>\n" +
//                 "         <password>" + password + "</password>\n" +
//                 "         <mode>APP</mode>\n" +
//                 "         <token>?</token>\n" +
//                 "      </run:login>\n" +
//                 "   </soapenv:Body>\n" +
//                 "</soapenv:Envelope>";

//         // Envoyer la requête SOAP
//         String soapResponse = sendSoapRequest(soapRequest);

//         // Parser la réponse SOAP
//         return Mono.just(parseSoapResponse(soapResponse));
//     }

//     private String sendSoapRequest(String soapRequest) {
//         StringWriter writer = new StringWriter();
//         StreamResult result = new StreamResult(writer);

//         webServiceTemplate.sendSourceAndReceiveToResult(
//                 "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS",
//                 new StreamSource(new StringReader(soapRequest)),
//                 new SoapActionCallback("http://runtime.services.cash.innov.sn/login"),
//                 result
//         );

//         return writer.toString();
//     }

//     private AuthResult parseSoapResponse(String soapResponse) {
//         // Vérifier si la réponse contient "<error>0</error>" (succès)
//         if (soapResponse.contains("<error>0</error>")) {
//             return new AuthResult(true, "Authentification réussie");
//         } else {
//             // Extraire le message d'erreur de la réponse SOAP
//             String errorMessage = extractErrorMessage(soapResponse);
//             return new AuthResult(false, errorMessage);
//         }
//     }

//     private String extractErrorMessage(String soapResponse) {
//         // Exemple simplifié pour extraire le message d'erreur
//         int startIndex = soapResponse.indexOf("<message>") + "<message>".length();
//         int endIndex = soapResponse.indexOf("</message>");
//         if (startIndex >= 0 && endIndex >= 0) {
//             return soapResponse.substring(startIndex, endIndex);
//         }
//         return "Erreur inconnue lors de l'authentification";
//     }
// }

































// // package com.innov4africa.api_gateway.service;

// // import com.innov4africa.api_gateway.model.AuthResult;
// // import org.springframework.stereotype.Service;
// // import org.springframework.ws.client.core.WebServiceTemplate;
// // import org.springframework.ws.soap.client.core.SoapActionCallback;
// // import reactor.core.publisher.Mono;

// // import javax.xml.transform.stream.StreamResult;
// // import javax.xml.transform.stream.StreamSource;
// // import java.io.StringReader;
// // import java.io.StringWriter;

// // @Service
// // public class IPayService {

// //     private final WebServiceTemplate webServiceTemplate;

// //     public IPayService(WebServiceTemplate webServiceTemplate) {
// //         this.webServiceTemplate = webServiceTemplate;
// //     }

// //     public Mono<AuthResult> authenticate(String email, String password) {
// //         // Construire la requête SOAP
// //         String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
// //                 "   <soapenv:Header/>\n" +
// //                 "   <soapenv:Body>\n" +
// //                 "      <run:login>\n" +
// //                 "         <login>" + email + "</login>\n" +
// //                 "         <password>" + password + "</password>\n" +
// //                 "         <mode>APP</mode>\n" +
// //                 "         <token>?</token>\n" +
// //                 "      </run:login>\n" +
// //                 "   </soapenv:Body>\n" +
// //                 "</soapenv:Envelope>";

// //         // Envoyer la requête SOAP
// //         String soapResponse = sendSoapRequest(soapRequest);

// //         // Parser la réponse SOAP
// //         return Mono.just(parseSoapResponse(soapResponse));
// //     }

// //     private String sendSoapRequest(String soapRequest) {
// //         StringWriter writer = new StringWriter();
// //         StreamResult result = new StreamResult(writer);

// //         webServiceTemplate.sendSourceAndReceiveToResult(
// //                 "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS",
// //                 new StreamSource(new StringReader(soapRequest)),
// //                 new SoapActionCallback("http://runtime.services.cash.innov.sn/login"),
// //                 result
// //         );

// //         return writer.toString();
// //     }

// //     private AuthResult parseSoapResponse(String soapResponse) {
        
// //         if (soapResponse.contains("<error>0</error>")) {
// //             return new AuthResult(true, "Authentification réussie");
// //         } else {
// //             String errorMessage = extractErrorMessage(soapResponse);
// //             return new AuthResult(false, errorMessage);
// //         }
// //     }

// //     private String extractErrorMessage(String soapResponse) {
        
// //         int startIndex = soapResponse.indexOf("<message>") + "<message>".length();
// //         int endIndex = soapResponse.indexOf("</message>");
// //         if (startIndex >= 0 && endIndex >= 0) {
// //             return soapResponse.substring(startIndex, endIndex);
// //         }
// //         return "Erreur inconnue lors de l'authentification";
// //     }
// // }


































// // package com.innov4africa.api_gateway.service;

// // import org.springframework.stereotype.Service;
// // import org.springframework.ws.client.core.WebServiceTemplate;
// // import org.springframework.ws.soap.client.core.SoapActionCallback;
// // import reactor.core.publisher.Mono;

// // import javax.xml.transform.stream.StreamResult;
// // import javax.xml.transform.stream.StreamSource;
// // import java.io.StringReader;
// // import java.io.StringWriter;

// // @Service
// // public class IPayService {

// //     private final WebServiceTemplate webServiceTemplate;

// //     public IPayService(WebServiceTemplate webServiceTemplate) {
// //         this.webServiceTemplate = webServiceTemplate;
// //     }

// //     public Mono<Boolean> authenticate(String email, String password) {
// //         // Construire la requête SOAP
// //         String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
// //                 "   <soapenv:Header/>\n" +
// //                 "   <soapenv:Body>\n" +
// //                 "      <run:login>\n" +
// //                 "         <login>" + email + "</login>\n" +
// //                 "         <password>" + password + "</password>\n" +
// //                 "         <mode>APP</mode>\n" +
// //                 "         <token>?</token>\n" +
// //                 "      </run:login>\n" +
// //                 "   </soapenv:Body>\n" +
// //                 "</soapenv:Envelope>";

// //         // Envoyer la requête SOAP
// //         String soapResponse = sendSoapRequest(soapRequest);

// //         // Parser la réponse SOAP
// //         return Mono.just(parseSoapResponse(soapResponse));
// //     }

// //     private String sendSoapRequest(String soapRequest) {
// //         StringWriter writer = new StringWriter();
// //         StreamResult result = new StreamResult(writer);

// //         webServiceTemplate.sendSourceAndReceiveToResult(
// //                 "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS",
// //                 new StreamSource(new StringReader(soapRequest)),
// //                 new SoapActionCallback("http://runtime.services.cash.innov.sn/login"),
// //                 result
// //         );

// //         return writer.toString();
// //     }

// //     private boolean parseSoapResponse(String soapResponse) {
// //         // Vérifier si la réponse contient "<error>0</error>" (succès)
// //         return soapResponse.contains("<error>0</error>");
// //     }
// // }








// // package com.innov4africa.api_gateway.service;

// // import org.springframework.stereotype.Service;
// // import org.springframework.web.reactive.function.client.WebClient;
// // import reactor.core.publisher.Mono;


// // import java.util.Map;

// // @Service
// // public class IPayService {

// //     // private final WebClient webClient;

// //     // public IPayService(WebClient.Builder webClientBuilder) {
// //     //     this.webClient = webClientBuilder.baseUrl("https://api.ipay.com").build();
// //     // }

// //     // public Mono<Boolean> authenticate(String email, String password) {
// //     //     return webClient.post()
// //     //             .uri("/authenticate")
// //     //             .bodyValue(Map.of("email", email, "password", password))
// //     //             .retrieve()
// //     //             .bodyToMono(Boolean.class)
// //     //             .onErrorReturn(false);
// //     // }
// //     public Mono<Boolean> authenticate(String email, String password) {
// //         // Simuler une réponse réussie pour tester
// //         return Mono.just(true);
// //     }
// // }

package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthResult;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

@Service
public class IPayService {

    private final WebServiceTemplate webServiceTemplate;

    public IPayService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public Mono<AuthResult> authenticate(String email, String password) {
        return Mono.fromCallable(() -> {
            SSLUtil.disableSslVerification();
            
            String soapRequest = buildSoapRequest(email, password);
            String soapResponse = sendSoapRequest(soapRequest);
            return parseSoapResponse(soapResponse);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> Mono.just(new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage())));
    }

    private String buildSoapRequest(String email, String password) {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
               "   <soapenv:Header/>\n" +
               "   <soapenv:Body>\n" +
               "      <run:login>\n" +
               "         <login>" + escapeXml(email) + "</login>\n" +
               "         <password>" + escapeXml(password) + "</password>\n" +
               "         <mode>APP</mode>\n" +
               "         <token>?</token>\n" +
               "      </run:login>\n" +
               "   </soapenv:Body>\n" +
               "</soapenv:Envelope>";
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    private String sendSoapRequest(String soapRequest) {
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);

        webServiceTemplate.sendSourceAndReceiveToResult(
                "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS",
                new StreamSource(new StringReader(soapRequest)),
                new SoapActionCallback("http://runtime.services.cash.innov.sn/login"),
                result
        );

        return writer.toString();
    }

    private AuthResult parseSoapResponse(String soapResponse) {
        if (soapResponse == null) {
            return new AuthResult(false, "Réponse SOAP vide");
        }
        
        if (soapResponse.contains("<error>0</error>")) {
            return new AuthResult(true, "Authentification réussie");
        } else {
            String errorMessage = extractErrorMessage(soapResponse);
            return new AuthResult(false, errorMessage);
        }
    }

    private String extractErrorMessage(String soapResponse) {
        int startIndex = soapResponse.indexOf("<message>");
        int endIndex = soapResponse.indexOf("</message>");
        
        if (startIndex >= 0 && endIndex >= 0 && endIndex > startIndex) {
            startIndex += "<message>".length();
            return soapResponse.substring(startIndex, endIndex);
        }
        return "Erreur inconnue lors de l'authentification";
    }
}

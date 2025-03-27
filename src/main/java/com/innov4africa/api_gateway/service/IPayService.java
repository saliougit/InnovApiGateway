// package com.innov4africa.api_gateway.service;

// import com.innov4africa.api_gateway.model.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.soap.client.core.SoapActionCallback;
// import reactor.core.publisher.Mono;
// import reactor.core.scheduler.Schedulers;

// @Service
// public class IPayService {
//     private static final Logger logger = LoggerFactory.getLogger(IPayService.class);
//     private final WebServiceTemplate webServiceTemplate;
//     private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

//     public IPayService(WebServiceTemplate webServiceTemplate) {
//         this.webServiceTemplate = webServiceTemplate;
//     }

//     public Mono<AuthResult> authenticate(String email, String password) {
//         return Mono.fromCallable(() -> {
//             try {
//                 logger.info("Tentative d'authentification pour l'utilisateur: {}", email);
//                 SSLUtil.disableSslVerification();
                
//                 LoginRequest loginRequest = new LoginRequest(email, password);
//                 SoapRequest soapRequest = new SoapRequest(loginRequest);
                
//                 logger.debug("Envoi de la requête SOAP à l'endpoint: {}", SOAP_ENDPOINT);
//                 Object response = webServiceTemplate.marshalSendAndReceive(
//                     SOAP_ENDPOINT,
//                     soapRequest,
//                     new SoapActionCallback("http://runtime.services.cash.innov.sn/login")
//                 );
                
//                 logger.debug("Réponse reçue du service SOAP: {}", response);
                
//                 if (response instanceof SoapResponse) {
//                     SoapResponse soapResponse = (SoapResponse) response;
//                     LoginResponse loginResponse = soapResponse.getLoginResponse();
//                     return parseResponse(loginResponse);
//                 } else {
//                     logger.error("Type de réponse inattendu: {}", response != null ? response.getClass() : "null");
//                     return new AuthResult(false, "Réponse inattendue du service SOAP");
//                 }
//             } catch (Exception e) {
//                 logger.error("Erreur lors de l'authentification", e);
//                 return new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage());
//             }
//         })
//         .subscribeOn(Schedulers.boundedElastic());
//     }

//     private AuthResult parseResponse(LoginResponse response) {
//         if (response == null) {
//             logger.error("Réponse SOAP vide reçue");
//             return new AuthResult(false, "Réponse SOAP vide");
//         }

//         if ("0".equals(response.getError())) {
//             logger.info("Authentification réussie pour l'utilisateur: {} {}", response.getPrenom(), response.getNom());
//             return new AuthResult(
//                 true, 
//                 String.format("Authentification réussie pour %s %s", response.getPrenom(), response.getNom()),
//                 response.getToken()
//             );
//         } else {
//             logger.warn("Échec de l'authentification. Message: {}", response.getMessage());
//             return new AuthResult(
//                 false, 
//                 response.getMessage() != null ? response.getMessage() : "Échec de l'authentification"
//             );
//         }
//     }
// }


// package com.innov4africa.api_gateway.service;

// import com.innov4africa.api_gateway.model.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;
// import org.springframework.ws.client.core.WebServiceTemplate;
// import org.springframework.ws.soap.client.core.SoapActionCallback;
// import reactor.core.publisher.Mono;
// import reactor.core.scheduler.Schedulers;

// import javax.net.ssl.*;
// import java.security.cert.X509Certificate;

// @Service
// public class IPayService {
 
//     private static final Logger logger = LoggerFactory.getLoggere(IPayService.class);
//     private final WebServiceTemplate webServiceTemplate;
//     private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

//     public IPayService(WebServiceTemplate webServiceTemplate) {
//         this.webServiceTemplate = webServiceTemplate;
//     }

//     public Mono<AuthResult> authenticate(String email, String password) {
//         return Mono.fromCallable(() -> {
//             try {
//                 logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

//                 // **IMPORTANT: Ne jamais désactiver la vérification SSL en production!**
//                 // C'est une faille de sécurité majeure.
//                 // Pour les environnements de test, configure un TrustStore correct.
//                 SSLUtil.disableSslVerification();

//                 LoginRequest loginRequest = new LoginRequest(email, password);
//                 SoapRequest soapRequest = new SoapRequest(loginRequest);

//                 logger.debug("Envoi de la requête SOAP à l'endpoint: {}", SOAP_ENDPOINT);
//                 Object response = webServiceTemplate.marshalSendAndReceive(
//                         SOAP_ENDPOINT,
//                         soapRequest,
//                         new SoapActionCallback("http://runtime.services.cash.innov.sn/login")
//                 );

//                 logger.debug("Réponse reçue du service SOAP: {}", response);

//                 if (response instanceof SoapResponse) {
//                     SoapResponse soapResponse = (SoapResponse) response;
//                     LoginResponse loginResponse = soapResponse.getLoginResponse();
//                     return parseResponse(loginResponse);
//                 } else {
//                     logger.error("Type de réponse inattendu: {}", response != null ? response.getClass() : "null");
//                     return new AuthResult(false, "Réponse inattendue du service SOAP");
//                 }
//             } catch (Exception e) {
//                 logger.error("Erreur lors de l'authentification", e);
//                 return new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage());
//             }
//         })
//                 .subscribeOn(Schedulers.boundedElastic());
//     }

//     private AuthResult parseResponse(LoginResponse response) {
//         if (response == null) {
//             logger.error("Réponse SOAP vide reçue");
//             return new AuthResult(false, "Réponse SOAP vide");
//         }

//         if ("0".equals(response.getError())) {
//             logger.info("Authentification réussie pour l'utilisateur: {} {}", response.getPrenom(), response.getNom());
//             return new AuthResult(
//                     true,
//                     String.format("Authentification réussie pour %s %s", response.getPrenom(), response.getNom()),
//                     response.getToken()
//             );
//         } else {
//             logger.warn("Échec de l'authentification. Message: {}", response.getMessage());
//             return new AuthResult(
//                     false,
//                     response.getMessage() != null ? response.getMessage() : "Échec de l'authentification"
//             );
//         }
//     }
// }


// package com.innov4africa.api_gateway.service;

// import java.io.StringReader;

// import javax.xml.parsers.DocumentBuilderFactory;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.w3c.dom.Document;
// import org.xml.sax.InputSource;

// import com.innov4africa.api_gateway.model.AuthResult;

// import reactor.core.publisher.Mono;
// import reactor.core.scheduler.Schedulers;

// @Service
// public class IPayService {

//     private static final Logger logger = LoggerFactory.getLogger(IPayService.class);

//     @Autowired
//     private WebClient.Builder webClientBuilder;

//     private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

//     public Mono<AuthResult> authenticate(String email, String password) {
//         return Mono.fromCallable(() -> {
//             try {
//                 logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

//                 SSLUtil.disableSslVerification();

//                 String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
//                         "   <soapenv:Header/>\n" +
//                         "   <soapenv:Body>\n" +
//                         "      <run:login>\n" +
//                         "         <login>" + email + "</login>\n" +
//                         "         <password>" + password + "</password>\n" +
//                         "         <mode>APP</mode>\n" +
//                         "         <token>?</token>\n" +
//                         "      </run:login>\n" +
//                         "   </soapenv:Body>\n" +
//                         "</soapenv:Envelope>";

//                 logger.debug("Envoi de la requête SOAP à l'endpoint: {}", SOAP_ENDPOINT);

//                 String response = webClientBuilder.build()
//                         .post()
//                         .uri(SOAP_ENDPOINT)
//                         .contentType(MediaType.TEXT_XML)
//                         .header("SOAPAction", "http://runtime.services.cash.innov.sn/login")
//                         .bodyValue(soapRequest)
//                         .retrieve()
//                         .bodyToMono(String.class)
//                         .block();

//                 logger.debug("Réponse reçue du service SOAP: {}", response);

//                 return parseResponse(response);

//             } catch (Exception e) {
//                 logger.error("Erreur lors de l'authentification", e);
//                 return new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage());
//             }
//         })
//                 .subscribeOn(Schedulers.boundedElastic());
//     }

//     private AuthResult parseResponse(String soapResponse) {
//         try {
//             Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
//                     .parse(new InputSource(new StringReader(soapResponse)));
//             doc.getDocumentElement().normalize();

//             String error = doc.getElementsByTagName("error").item(0).getTextContent();
//             String message = doc.getElementsByTagName("message").item(0).getTextContent();
//             String token = doc.getElementsByTagName("token").item(0).getTextContent();
//             String nom = doc.getElementsByTagName("nom").item(0).getTextContent();
//             String prenom = doc.getElementsByTagName("prenom").item(0).getTextContent();

//             if ("0".equals(error)) {
//                 logger.info("Authentification réussie pour l'utilisateur: {} {}", prenom, nom);
//                 return new AuthResult(
//                         true,
//                         String.format("Authentification réussie pour %s %s", prenom, nom),
//                         token
//                 );
//             } else {
//                 logger.warn("Échec de l'authentification. Message: {}", message);
//                 return new AuthResult(
//                         false,
//                         message != null ? message : "Échec de l'authentification"
//                 );
//             }

//         } catch (Exception e) {
//             logger.error("Erreur lors du parsing de la réponse SOAP", e);
//             return new AuthResult(false, "Erreur lors du parsing de la réponse SOAP: " + e.getMessage());
//         }
//     }
// }


// package com.innov4africa.api_gateway.service;

// import com.innov4africa.api_gateway.model.AuthResult;
// import io.netty.handler.ssl.SslContext;
// import io.netty.handler.ssl.SslContextBuilder;
// import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.client.reactive.ReactorClientHttpConnector;
// import org.springframework.http.HttpStatusCode;
// import org.springframework.http.MediaType;
// import org.springframework.stereotype.Service;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;
// import reactor.core.scheduler.Schedulers;
// import org.w3c.dom.Document;
// import org.w3c.dom.Node;
// import org.w3c.dom.NodeList;
// import org.xml.sax.InputSource;
// import javax.net.ssl.SSLException;
// import javax.xml.parsers.DocumentBuilderFactory;
// import java.io.StringReader;
// import java.util.HashMap;
// import java.util.Map;

// import reactor.netty.http.client.HttpClient;

// // @Service
// // public class IPayService {

// //     private static final Logger logger = LoggerFactory.getLogger(IPayService.class);

// //     private final WebClient webClient;

// //     private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

// //     public IPayService() throws SSLException {
// //         SslContext sslContext = SslContextBuilder.forClient()
// //                 .trustManager(InsecureTrustManagerFactory.INSTANCE)
// //                 .build();

// //         HttpClient httpClient = HttpClient.create()
// //                 .secure(t -> t.sslContext(sslContext));

// //         this.webClient = WebClient.builder()
// //                 .clientConnector(new ReactorClientHttpConnector(httpClient))
// //                 .build();
// //     }

// //     public Mono<AuthResult> authenticate(String email, String password) {
// //         return Mono.fromCallable(() -> {
// //             try {
// //                 logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

// //                 String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:run=\"http://runtime.services.cash.innov.sn/\">\n" +
// //                         "   <soapenv:Header/>\n" +
// //                         "   <soapenv:Body>\n" +
// //                         "      <run:login>\n" +
// //                         "         <login>" + email + "</login>\n" +
// //                         "         <password>" + password + "</password>\n" +
// //                         "         <mode>APP</mode>\n" +
// //                         "         <token>?</token>\n" +
// //                         "      </run:login>\n" +
// //                         "   </soapenv:Body>\n" +
// //                         "</soapenv:Envelope>";

// //                 logger.debug("Envoi de la requête SOAP à l'endpoint: {}", SOAP_ENDPOINT);

// //                 String response = webClient.post()
// //                         .uri(SOAP_ENDPOINT)
// //                         .contentType(MediaType.TEXT_XML)
// //                         .header("SOAPAction", "http://runtime.services.cash.innov.sn/login")
// //                         .bodyValue(soapRequest)
// //                         .retrieve()
// //                         .bodyToMono(String.class)
// //                         .block();

// //                 logger.debug("Réponse reçue du service SOAP: {}", response);

// //                 return parseResponse(response);

// //             } catch (Exception e) {
// //                 logger.error("Erreur lors de l'authentification", e);
// //                 return new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage());
// //             }
// //         })
// //                 .subscribeOn(Schedulers.boundedElastic());
// //     }

// //     private AuthResult parseResponse(String soapResponse) {
// //         try {
// //             Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
// //                     .parse(new InputSource(new StringReader(soapResponse)));
// //             doc.getDocumentElement().normalize();

// //             String error = doc.getElementsByTagName("error").item(0).getTextContent();
// //             String message = doc.getElementsByTagName("message").item(0).getTextContent();
// //             String token = doc.getElementsByTagName("token").item(0).getTextContent();
// //             String nom = doc.getElementsByTagName("nom").item(0).getTextContent();
// //             String prenom = doc.getElementsByTagName("prenom").item(0).getTextContent();

// //             if ("0".equals(error)) {
// //                 logger.info("Authentification réussie pour l'utilisateur: {} {}", prenom, nom);
// //                 return new AuthResult(
// //                         true,
// //                         String.format("Authentification réussie pour %s %s", prenom, nom),
// //                         token
// //                 );
// //             } else {
// //                 logger.warn("Échec de l'authentification. Message: {}", message);
// //                 return new AuthResult(
// //                         false,
// //                         message != null ? message : "Échec de l'authentification"
// //                 );
// //             }

// //         } catch (Exception e) {
// //             logger.error("Erreur lors du parsing de la réponse SOAP", e);
// //             return new AuthResult(false, "Erreur lors du parsing de la réponse SOAP: " + e.getMessage());
// //         }
// //     }
// // }

// @Service
// public class IPayService {
//     private static final Logger logger = LoggerFactory.getLogger(IPayService.class);
//     private final WebClient webClient;
//     private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

//     public IPayService() throws SSLException {
//         SslContext sslContext = SslContextBuilder.forClient()
//                 .trustManager(InsecureTrustManagerFactory.INSTANCE)
//                 .build();

//         HttpClient httpClient = HttpClient.create()
//                 .secure(t -> t.sslContext(sslContext))
//                 .wiretap(true); // Active le logging des requêtes/réponses

//         this.webClient = WebClient.builder()
//                 .clientConnector(new ReactorClientHttpConnector(httpClient))
//                 .build();
//     }

//     public Mono<AuthResult> authenticate(String email, String password) {
//         return Mono.fromCallable(() -> {
//             try {
//                 logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

//                 // Utilisation d'un template XML plus propre
//                 String soapRequest = """
//                     <soapenv:Envelope 
//                         xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
//                         xmlns:run="http://runtime.services.cash.innov.sn/">
//                        <soapenv:Header/>
//                        <soapenv:Body>
//                           <run:login>
//                              <login>%s</login>
//                              <password>%s</password>
//                              <mode>APP</mode>
//                              <token>?</token>
//                           </run:login>
//                        </soapenv:Body>
//                     </soapenv:Envelope>
//                     """.formatted(email, password);

//                 logger.debug("Requête SOAP:\n{}", soapRequest);

//                 String response = webClient.post()
//                         .uri(SOAP_ENDPOINT)
//                         .contentType(MediaType.TEXT_XML)
//                         .header("SOAPAction", "\"http://runtime.services.cash.innov.sn/login\"") // Notez les guillemets
//                         .accept(MediaType.TEXT_XML)
//                         .bodyValue(soapRequest)
//                         .retrieve()
//                         .onStatus(HttpStatusCode::isError, clientResponse -> {
//                             logger.error("Erreur HTTP: {}", clientResponse.statusCode());
//                             return clientResponse.bodyToMono(String.class)
//                                 .flatMap(body -> {
//                                     logger.error("Corps de l'erreur: {}", body);
//                                     return Mono.error(new RuntimeException("Erreur SOAP: " + body));
//                                 });
//                         })
//                         .bodyToMono(String.class)
//                         .block();

//                 logger.debug("Réponse SOAP:\n{}", response);
//                 return parseResponse(response);

//             } catch (Exception e) {
//                 logger.error("Erreur lors de l'authentification", e);
//                 return new AuthResult(false, "Erreur technique: " + e.getMessage());
//             }
//         }).subscribeOn(Schedulers.boundedElastic());
//     }

//     private AuthResult parseResponse(String soapResponse) {
//         try {
//             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//             factory.setNamespaceAware(true); // Important pour les requêtes SOAP
//             Document doc = factory.newDocumentBuilder()
//                     .parse(new InputSource(new StringReader(soapResponse)));

//             NodeList returnNodes = doc.getElementsByTagNameNS("http://runtime.services.cash.innov.sn/", "return");
//             if (returnNodes.getLength() == 0) {
//                 throw new RuntimeException("Balise 'return' introuvable dans la réponse");
//             }

//             Node returnNode = returnNodes.item(0);
//             Map<String, String> responseData = new HashMap<>();
//             NodeList childNodes = returnNode.getChildNodes();

//             for (int i = 0; i < childNodes.getLength(); i++) {
//                 Node node = childNodes.item(i);
//                 if (node.getNodeType() == Node.ELEMENT_NODE) {
//                     responseData.put(node.getNodeName(), node.getTextContent());
//                 }
//             }

//             String error = responseData.get("error");
//             String message = responseData.get("message");
//             String token = responseData.get("token");
//             String nom = responseData.get("nom");
//             String prenom = responseData.get("prenom");

//             if ("0".equals(error)) {
//                 logger.info("Authentification réussie pour {} {}", prenom, nom);
//                 return new AuthResult(true, message, token, nom, prenom);
//             } else {
//                 logger.warn("Échec d'authentification: {}", message);
//                 return new AuthResult(false, message);
//             }

//         } catch (Exception e) {
//             logger.error("Erreur de parsing XML", e);
//             return new AuthResult(false, "Erreur de traitement de la réponse: " + e.getMessage());
//         }
//     }
// }




package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthResult;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.net.ssl.SSLException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import java.io.StringReader;
import reactor.netty.http.client.HttpClient;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.springframework.http.HttpStatusCode;
import java.util.HashMap;
import java.util.Map;

@Service
public class IPayService {

    private static final Logger logger = LoggerFactory.getLogger(IPayService.class);
    private final WebClient webClient;
    private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

    public IPayService() throws SSLException {
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(t -> t.sslContext(sslContext))
                .wiretap(true); // Active le logging des requêtes/réponses

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // public Mono<AuthResult> authenticate(String email, String password) {
    //     return Mono.fromCallable(() -> {
    //         try {
    //             logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

    //             // Utilisation d'un template XML plus propre
    //             String soapRequest = """
    //                 <soapenv:Envelope 
    //                     xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
    //                     xmlns:run="http://runtime.services.cash.innov.sn/">
    //                    <soapenv:Header/>
    //                    <soapenv:Body>
    //                       <run:login>
    //                          <login>%s</login>
    //                          <password>%s</password>
    //                          <mode>APP</mode>
    //                          <token>?</token>
    //                       </run:login>
    //                    </soapenv:Body>
    //                 </soapenv:Envelope>
    //                 """.formatted(email, password);

    //             logger.debug("Requête SOAP:\n{}", soapRequest);

    //             String response = webClient.post()
    //                     .uri(SOAP_ENDPOINT)
    //                     .contentType(MediaType.TEXT_XML)
    //                     // .header("SOAPAction", "\"http://runtime.services.cash.innov.sn/login\"")
    //                     .accept(MediaType.TEXT_XML)
    //                     .bodyValue(soapRequest)
    //                     .retrieve()
    //                      .onStatus(HttpStatusCode::isError, clientResponse -> {
    //                         logger.error("Erreur HTTP: {}", clientResponse.statusCode());
    //                         return clientResponse.bodyToMono(String.class)
    //                             .flatMap(body -> {
    //                                 logger.error("Corps de l'erreur: {}", body);
    //                                 return Mono.error(new RuntimeException("Erreur SOAP: " + body));
    //                             });
    //                     })
    //                     .bodyToMono(String.class)
    //                     .block();

    //             logger.debug("Réponse SOAP:\n{}", response);
    //              return new AuthResult(false, response);
    //        //     return parseResponse(response);

    //         } catch (Exception e) {
    //             logger.error("Erreur lors de l'authentification", e);
    //             return new AuthResult(false, "Erreur technique: " + e.getMessage());
    //         }
    //     }).subscribeOn(Schedulers.boundedElastic());
    // }

    // private AuthResult parseResponse(String soapResponse) {
    //     try {
    //         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //         factory.setNamespaceAware(true); // Important pour les requêtes SOAP
    //         Document doc = factory.newDocumentBuilder()
    //                 .parse(new InputSource(new StringReader(soapResponse)));

    //         NodeList returnNodes = doc.getElementsByTagNameNS("http://runtime.services.cash.innov.sn/", "return");
    //         if (returnNodes.getLength() == 0) {
    //             throw new RuntimeException("Balise 'return' introuvable dans la réponse");
    //         }

    //         Node returnNode = returnNodes.item(0);
    //         Map<String, String> responseData = new HashMap<>();
    //         NodeList childNodes = returnNode.getChildNodes();

    //         for (int i = 0; i < childNodes.getLength(); i++) {
    //             Node node = childNodes.item(i);
    //             if (node.getNodeType() == Node.ELEMENT_NODE) {
    //                 responseData.put(node.getNodeName(), node.getTextContent());
    //             }
    //         }

    //         String error = responseData.get("error");
    //         String message = responseData.get("message");
    //         String token = responseData.get("token");
    //         String nom = responseData.get("nom");
    //         String prenom = responseData.get("prenom");

    //         if ("0".equals(error)) {
    //             logger.info("Authentification réussie pour {} {}", prenom, nom);
    //             return new AuthResult(true, message, token, nom, prenom);
    //         } else {
    //             logger.warn("Échec d'authentification: {}", message);
    //             return new AuthResult(false, message);
    //         }

    //     } catch (Exception e) {
    //         logger.error("Erreur de parsing XML", e);
    //         return new AuthResult(false, "Erreur de traitement de la réponse: " + e.getMessage());
    //     }
    // }

        public Mono<AuthResult> authenticate(String email, String password) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative d'authentification pour l'utilisateur: {}", email);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <run:login>
                            <login>%s</login>
                            <password>%s</password>
                            <mode>APP</mode>
                            <token>?</token>
                        </run:login>
                    </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(email, password);

                logger.debug("Requête SOAP:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP:\n{}", response);
                return parseResponse(response);

            } catch (Exception e) {
                logger.error("Erreur lors de l'authentification", e);
                return new AuthResult(false, "Erreur technique: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private AuthResult parseResponse(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapResponse)));

            // Utilisation de XPath pour une extraction plus robuste
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                public String getNamespaceURI(String prefix) {
                    return "http://runtime.services.cash.innov.sn/";
                }
                public String getPrefix(String namespaceURI) { return null; }
                public java.util.Iterator<String> getPrefixes(String namespaceURI) { return null; }
            });

            String error = xpath.evaluate("//return/error", doc);
            String message = xpath.evaluate("//return/message", doc);
            String token = xpath.evaluate("//return/token", doc);
            String nom = xpath.evaluate("//return/nom", doc);
            String prenom = xpath.evaluate("//return/prenom", doc);

            if ("0".equals(error)) {
                logger.info("Authentification réussie pour {} {}", prenom, nom);
                return new AuthResult(true, message, token, nom, prenom);
            } else {
                logger.warn("Échec d'authentification: {}", message);
                return new AuthResult(false, message);
            }
        } catch (Exception e) {
            logger.error("Erreur de parsing XML", e);
            return new AuthResult(false, "Erreur de traitement de la réponse: " + e.getMessage());
        }
    }
}
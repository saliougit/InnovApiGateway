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

    // private AuthResult parseResponse(String soapResponse) {
    //     try {
    //         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //         factory.setNamespaceAware(true);
    //         Document doc = factory.newDocumentBuilder()
    //                 .parse(new InputSource(new StringReader(soapResponse)));

    //         // Utilisation de XPath pour une extraction plus robuste
    //         XPath xpath = XPathFactory.newInstance().newXPath();
    //         xpath.setNamespaceContext(new NamespaceContext() {
    //             public String getNamespaceURI(String prefix) {
    //                 return "http://runtime.services.cash.innov.sn/";
    //             }
    //             public String getPrefix(String namespaceURI) { return null; }
    //             public java.util.Iterator<String> getPrefixes(String namespaceURI) { return null; }
    //         });

    //         String error = xpath.evaluate("//return/error", doc);
    //         String message = xpath.evaluate("//return/message", doc);
    //         String token = xpath.evaluate("//return/token", doc);
    //         String nom = xpath.evaluate("//return/nom", doc);
    //         String prenom = xpath.evaluate("//return/prenom", doc);

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
    private AuthResult parseResponse(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapResponse)));
    
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
    
            // Cas particulier pour le code d'erreur 13 (session existante)
            if ("0".equals(error) || "13".equals(error)) {
                logger.info("Authentification réussie ou session existante pour {} {}", prenom, nom);
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
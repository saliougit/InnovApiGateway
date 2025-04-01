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
                .wiretap(true);

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

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

    public Mono<String> getSolde(String telephone, String ipayToken) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Récupération du solde pour le téléphone: {}", telephone);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <run:getSoldeByTelephone>
                            <telephone>%s</telephone>
                        </run:getSoldeByTelephone>
                    </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(telephone);

                logger.debug("Requête SOAP pour le solde:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + ipayToken)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour le solde:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors de la récupération du solde", e);
                throw new RuntimeException("Erreur technique lors de la récupération du solde: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getUOByCellular(String idSession, String cellular) {
        return Mono.fromCallable(() -> {
            try {
                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:getUOByCellular>
                             <idSession>%s</idSession>
                             <cellular>%s</cellular>
                          </run:getUOByCellular>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(idSession, cellular);
    
                return webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + idSession) // Utilise le token comme idSession
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de l'appel getUOByCellular: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> getHistorySolde(String idSession, String accountId) {
        return Mono.fromCallable(() -> {
            String soapRequest = """
                <soapenv:Envelope 
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                    xmlns:run="http://runtime.services.cash.innov.sn/">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <run:getHistorySolde>
                         <idSession>%s</idSession>
                         <AccountId>%s</AccountId>
                      </run:getHistorySolde>
                   </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(idSession, accountId);
    
            return webClient.post()
                    .uri(SOAP_ENDPOINT)
                    .contentType(MediaType.TEXT_XML)
                    .header("Authorization", "Bearer " + idSession)
                    .bodyValue(soapRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Déconnecte un utilisateur de la plateforme IPay en utilisant son token de session
     * @param idSession Le token de session IPay de l'utilisateur à déconnecter
     * @return Une réponse SOAP contenant le résultat de la déconnexion
     */
    public Mono<String> deconnexionUser(String idSession) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative de déconnexion pour la session: {}", idSession);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:deconnexionUser>
                             <idSession>%s</idSession>
                          </run:deconnexionUser>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(idSession);

                logger.debug("Requête SOAP de déconnexion:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP de déconnexion:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors de la déconnexion", e);
                throw new RuntimeException("Erreur technique lors de la déconnexion: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Récupère la liste des opérations effectuées sur un compte
     * @param sessionId Le token de session IPay
     * @param accountId L'identifiant du compte
     * @return Une réponse SOAP contenant la liste des opérations et leur nombre total
     */
    public Mono<String> getOperationCompte(String sessionId, String accountId) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Récupération des opérations pour le compte: {}", accountId);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:getOperationCompte>
                             <sessionId>%s</sessionId>
                             <accountId>%s</accountId>
                          </run:getOperationCompte>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(sessionId, accountId);

                logger.debug("Requête SOAP pour les opérations:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + sessionId)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour les opérations:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors de la récupération des opérations", e);
                throw new RuntimeException("Erreur technique lors de la récupération des opérations: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Récupère toutes les notifications d'un utilisateur
     * @param sessionId Le token de session IPay
     * @param uoId L'identifiant de l'utilisateur
     * @return Une réponse SOAP contenant la liste des notifications
     */
    public Mono<String> getAllNotif(String sessionId, String uoId) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Récupération des notifications pour l'utilisateur: {}", uoId);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:getAllNotif>
                             <idSession>%s</idSession>
                             <uoId>%s</uoId>
                          </run:getAllNotif>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(sessionId, uoId);

                logger.debug("Requête SOAP pour les notifications:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + sessionId)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour les notifications:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors de la récupération des notifications", e);
                throw new RuntimeException("Erreur technique lors de la récupération des notifications: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
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
            String telephone = xpath.evaluate("//return/telephone", doc);
            String iduser = xpath.evaluate("//return/iduser", doc);
    
            if ("0".equals(error) || "13".equals(error)) {
                logger.info("Authentification réussie ou session existante pour {} {}", prenom, nom);
                AuthResult result = new AuthResult(true, message, token, nom, prenom, telephone, iduser, null);
                result.setTelephone(telephone);
                result.setIduser(iduser);
                return result;
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
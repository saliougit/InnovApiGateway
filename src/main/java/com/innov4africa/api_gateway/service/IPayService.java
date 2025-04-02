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
    
    /**
     * Effectue un virement compte à compte
     * @param sessionId Le token de session IPay
     * @param montant Le montant à transférer
     * @param commission La commission du transfert (0 par défaut)
     * @param idAccountEnvoyeur L'identifiant du compte émetteur
     * @param idAccountBeneficiary L'identifiant du compte bénéficiaire
     * @param objet L'objet du virement (optionnel)
     * @param commissionRetrait La commission de retrait (0 par défaut)
     * @return Une réponse SOAP contenant le résultat du virement
     */
    public Mono<String> w2wVirementAccount(
            String sessionId, 
            String montant, 
            String commission, 
            String idAccountEnvoyeur,
            String idAccountBeneficiary, 
            String objet,
            String commissionRetrait) {
        
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative de virement du compte {} vers le compte {}, montant: {}", 
                    idAccountEnvoyeur, idAccountBeneficiary, montant);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:W2WVirementAccount>
                             <sessionId>%s</sessionId>
                             <montant>%s</montant>
                             <commission>%s</commission>
                             <idAccountEnvoyeur>%s</idAccountEnvoyeur>
                             <idAccountBeneficiary>%s</idAccountBeneficiary>
                             <objet>%s</objet>
                             <commissionRetrait>%s</commissionRetrait>
                          </run:W2WVirementAccount>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(sessionId, montant, commission, idAccountEnvoyeur, 
                        idAccountBeneficiary, objet, commissionRetrait);

                logger.debug("Requête SOAP pour le virement:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + sessionId)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour le virement:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors du virement", e);
                throw new RuntimeException("Erreur technique lors du virement: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Envoi un code de paiement par SMS
     * @param envoyeurTel Le numéro de téléphone de l'expéditeur
     * @param beneficiaireTel Le numéro de téléphone du bénéficiaire
     * @param montant Le montant du paiement
     * @param numeros Numéro de référence (optionnel)
     * @param nom Le nom de l'utilisateur
     * @param prenom Le prénom de l'utilisateur
     * @return Une réponse SOAP contenant le résultat de l'opération
     */
    public Mono<String> smsPayCodeGFact(
            String envoyeurTel, 
            String beneficiaireTel, 
            String montant, 
            String numeros,
            String nom,
            String prenom) {
        
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative d'envoi de code de paiement par SMS de {} à {}, montant: {}", 
                    envoyeurTel, beneficiaireTel, montant);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:SmsPayCodeG_FACT>
                             <envoyeurtel>%s</envoyeurtel>
                             <beneficiairetel>%s</beneficiairetel>
                             <montant>%s</montant>
                             <numeros>%s</numeros>
                             <nom>%s</nom>
                             <prenom>%s</prenom>
                          </run:SmsPayCodeG_FACT>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(envoyeurTel, beneficiaireTel, montant, numeros, nom, prenom);

                logger.debug("Requête SOAP pour l'envoi de code de paiement:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour l'envoi de code de paiement:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi du code de paiement", e);
                throw new RuntimeException("Erreur technique lors de l'envoi du code de paiement: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Effectue un paiement de facture SDE
     * @param sessionId Le token de session IPay
     * @param numeroPolice Le numéro de police d'abonnement
     * @param numeroFacture Le numéro de facture
     * @param referenceClient La référence client
     * @param montant Le montant à payer
     * @param commission La commission
     * @param cellular Le numéro de téléphone du client
     * @param commagent La commission de l'agent
     * @return Une réponse SOAP contenant le résultat du paiement
     */
    public Mono<String> paiementSDE(
            String sessionId, 
            String numeroPolice, 
            String numeroFacture, 
            String referenceClient,
            String montant, 
            String commission, 
            String cellular, 
            String commagent) {
        
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative de paiement SDE pour la référence: {}, police: {}, facture: {}, montant: {}", 
                    referenceClient, numeroPolice, numeroFacture, montant);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                       <soapenv:Header/>
                       <soapenv:Body>
                          <run:paiementSDE>
                             <idSession>%s</idSession>
                             <numeroPolice>%s</numeroPolice>
                             <numeroFacture>%s</numeroFacture>
                             <referenceClient>%s</referenceClient>
                             <montant>%s</montant>
                             <commission>%s</commission>
                             <cellular>%s</cellular>
                             <commagent>%s</commagent>
                          </run:paiementSDE>
                       </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(sessionId, numeroPolice, numeroFacture, referenceClient, 
                        montant, commission, cellular, commagent);

                logger.debug("Requête SOAP pour le paiement SDE:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + sessionId)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour le paiement SDE:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors du paiement SDE", e);
                throw new RuntimeException("Erreur technique lors du paiement SDE: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

        /**
     * Effectue un paiement de facture Senelec
     * @param sessionId Le token de session IPay
     * @param numeroPolice Le numéro de police d'abonnement
     * @param numeroFacture Le numéro de facture
     * @param montant Le montant à payer
     * @param commission La commission
     * @param cellular Le numéro de téléphone du client
     * @param commagent La commission de l'agent
     * @return Une réponse SOAP contenant le résultat du paiement
     */
    public Mono<String> paiementSenelec(
            String sessionId, 
            String numeroPolice, 
            String numeroFacture, 
            String montant, 
            String commission, 
            String cellular, 
            String commagent) {
        
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative de paiement Senelec pour la police: {}, facture: {}, montant: {}", 
                    numeroPolice, numeroFacture, montant);

                String soapRequest = """
                    <soapenv:Envelope 
                        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                        xmlns:run="http://runtime.services.cash.innov.sn/">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <run:paiementSenelec>
                            <idSession>%s</idSession>
                            <numeroPolice>%s</numeroPolice>
                            <numeroFacture>%s</numeroFacture>
                            <montant>%s</montant>
                            <commission>%s</commission>
                            <cellular>%s</cellular>
                            <commagent>%s</commagent>
                        </run:paiementSenelec>
                    </soapenv:Body>
                    </soapenv:Envelope>
                    """.formatted(sessionId, numeroPolice, numeroFacture, montant, 
                        commission, cellular, commagent);

                logger.debug("Requête SOAP pour le paiement Senelec:\n{}", soapRequest);

                String response = webClient.post()
                        .uri(SOAP_ENDPOINT)
                        .contentType(MediaType.TEXT_XML)
                        .header("Authorization", "Bearer " + sessionId)
                        .accept(MediaType.TEXT_XML)
                        .bodyValue(soapRequest)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                logger.debug("Réponse SOAP pour le paiement Senelec:\n{}", response);
                return response;

            } catch (Exception e) {
                logger.error("Erreur lors du paiement Senelec", e);
                throw new RuntimeException("Erreur technique lors du paiement Senelec: " + e.getMessage());
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
            /**
     * Effectue un paiement Woyofal
     * @param sessionId Le token de session IPay
     * @param numeroPolice Le numéro de police d'abonnement
     * @param numeroTelephone Le numéro de téléphone associé au compteur
     * @param montant Le montant à payer
     * @param frais Les frais
     * @param commission La commission
     * @param cellular Le numéro de téléphone du client
     * @return Une réponse SOAP contenant le résultat du paiement
     */
    public Mono<String> paiementWoyofal(
        String sessionId, 
        String numeroPolice, 
        String numeroTelephone, 
        String montant, 
        String frais,
        String commission, 
        String cellular) {

    return Mono.fromCallable(() -> {
        try {
            logger.info("Tentative de paiement Woyofal pour la police: {}, téléphone: {}, montant: {}", 
                numeroPolice, numeroTelephone, montant);

            String soapRequest = """
                <soapenv:Envelope 
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                    xmlns:run="http://runtime.services.cash.innov.sn/">
                <soapenv:Header/>
                <soapenv:Body>
                    <run:paiementWoyofal>
                        <idSession>%s</idSession>
                        <numeroPolice>%s</numeroPolice>
                        <numeroTelephone>%s</numeroTelephone>
                        <montant>%s</montant>
                        <frais>%s</frais>
                        <commission>%s</commission>
                        <cellular>%s</cellular>
                    </run:paiementWoyofal>
                </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(sessionId, numeroPolice, numeroTelephone, montant, 
                    frais, commission, cellular);

            logger.debug("Requête SOAP pour le paiement Woyofal:\n{}", soapRequest);

            String response = webClient.post()
                    .uri(SOAP_ENDPOINT)
                    .contentType(MediaType.TEXT_XML)
                    .header("Authorization", "Bearer " + sessionId)
                    .accept(MediaType.TEXT_XML)
                    .bodyValue(soapRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("Réponse SOAP pour le paiement Woyofal:\n{}", response);
            return response;

        } catch (Exception e) {
            logger.error("Erreur lors du paiement Woyofal", e);
            throw new RuntimeException("Erreur technique lors du paiement Woyofal: " + e.getMessage());
        }
    }).subscribeOn(Schedulers.boundedElastic());
    }


        /**
     * Effectue un paiement sur la plateforme iShop
     * @param numeros Le numéro de référence
     * @param montant Le montant à payer
     * @param order L'identifiant de la commande
     * @param code Le code de paiement
     * @return Une réponse SOAP contenant le résultat du paiement
     */
    public Mono<String> paymentIshop(
        String numeros, 
        String montant, 
        String order, 
        String code) {

    return Mono.fromCallable(() -> {
        try {
            logger.info("Tentative de paiement iShop pour la commande: {}, montant: {}", order, montant);

            String soapRequest = """
                <soapenv:Envelope 
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" 
                    xmlns:run="http://runtime.services.cash.innov.sn/">
                <soapenv:Header/>
                <soapenv:Body>
                    <run:paymentIshop>
                        <numeros>%s</numeros>
                        <montant>%s</montant>
                        <order>%s</order>
                        <code>%s</code>
                    </run:paymentIshop>
                </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(numeros, montant, order, code);

            logger.debug("Requête SOAP pour le paiement iShop:\n{}", soapRequest);

            String response = webClient.post()
                    .uri(SOAP_ENDPOINT)
                    .contentType(MediaType.TEXT_XML)
                    .accept(MediaType.TEXT_XML)
                    .bodyValue(soapRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            logger.debug("Réponse SOAP pour le paiement iShop:\n{}", response);
            return response;

        } catch (Exception e) {
            logger.error("Erreur lors du paiement iShop", e);
            throw new RuntimeException("Erreur technique lors du paiement iShop: " + e.getMessage());
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
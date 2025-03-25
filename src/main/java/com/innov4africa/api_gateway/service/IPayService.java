package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class IPayService {
    private static final Logger logger = LoggerFactory.getLogger(IPayService.class);
    private final WebServiceTemplate webServiceTemplate;
    private static final String SOAP_ENDPOINT = "https://ibusinesscompanies.com:8443/cash-ws/CashWalletServiceWS";

    public IPayService(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public Mono<AuthResult> authenticate(String email, String password) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Tentative d'authentification pour l'utilisateur: {}", email);
                SSLUtil.disableSslVerification();
                
                LoginRequest loginRequest = new LoginRequest(email, password);
                SoapRequest soapRequest = new SoapRequest(loginRequest);
                
                logger.debug("Envoi de la requête SOAP à l'endpoint: {}", SOAP_ENDPOINT);
                Object response = webServiceTemplate.marshalSendAndReceive(
                    SOAP_ENDPOINT,
                    soapRequest,
                    new SoapActionCallback("http://runtime.services.cash.innov.sn/login")
                );
                
                logger.debug("Réponse reçue du service SOAP: {}", response);
                
                if (response instanceof SoapResponse) {
                    SoapResponse soapResponse = (SoapResponse) response;
                    LoginResponse loginResponse = soapResponse.getLoginResponse();
                    return parseResponse(loginResponse);
                } else {
                    logger.error("Type de réponse inattendu: {}", response != null ? response.getClass() : "null");
                    return new AuthResult(false, "Réponse inattendue du service SOAP");
                }
            } catch (Exception e) {
                logger.error("Erreur lors de l'authentification", e);
                return new AuthResult(false, "Erreur lors de l'authentification: " + e.getMessage());
            }
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    private AuthResult parseResponse(LoginResponse response) {
        if (response == null) {
            logger.error("Réponse SOAP vide reçue");
            return new AuthResult(false, "Réponse SOAP vide");
        }

        if ("0".equals(response.getError())) {
            logger.info("Authentification réussie pour l'utilisateur: {} {}", response.getPrenom(), response.getNom());
            return new AuthResult(
                true, 
                String.format("Authentification réussie pour %s %s", response.getPrenom(), response.getNom()),
                response.getToken()
            );
        } else {
            logger.warn("Échec de l'authentification. Message: {}", response.getMessage());
            return new AuthResult(
                false, 
                response.getMessage() != null ? response.getMessage() : "Échec de l'authentification"
            );
        }
    }
}

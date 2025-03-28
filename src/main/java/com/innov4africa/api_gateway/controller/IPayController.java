package com.innov4africa.api_gateway.controller;

import com.innov4africa.api_gateway.model.HistoryItem;
import com.innov4africa.api_gateway.model.HistoryResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.model.SoldeResponse;
import com.innov4africa.api_gateway.model.UO;
import com.innov4africa.api_gateway.model.UOResponse;
import com.innov4africa.api_gateway.service.IPayService;
import com.innov4africa.api_gateway.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/ipay")
public class IPayController {

    private static final Logger logger = LoggerFactory.getLogger(IPayController.class);
    
    @Autowired
    private IPayService ipayService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/solde")
    public Mono<ResponseEntity<SoldeResponse>> getSolde(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token d'authentification manquant", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Format de token invalide", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token invalide ou expiré", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new SoldeResponse("error", "Token incomplet", "0.00", 
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande de solde pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay
        return ipayService.getSolde(telephone, ipayToken)
            .flatMap(this::handleSoapResponse)
            .onErrorResume(this::handleError);
    }

    @GetMapping("/uo")
    public Mono<ResponseEntity<UOResponse>> getUOByCellular(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return buildUnauthorizedResponse(
                new UOResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse(
                new UOResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande d'UO pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay
        return ipayService.getUOByCellular(ipayToken, telephone)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("0".equals(error)) {
                        // Si uo existe dans la réponse
                        String uoError = xpath.evaluate("//return/uo/error", doc);
                        if (!uoError.isEmpty()) {
                            UO uo = new UO(
                                uoError,
                                xpath.evaluate("//return/uo/id", doc),
                                xpath.evaluate("//return/uo/nom", doc),
                                xpath.evaluate("//return/uo/numTel", doc),
                                xpath.evaluate("//return/uo/prenom", doc),
                                xpath.evaluate("//return/uo/type", doc)
                            );
                            return Mono.just(ResponseEntity.ok(
                                new UOResponse("success", message, uo, 
                                    List.of(new ServiceStatus("i-pay", true, "UO trouvé")))
                            ));
                        }
                        return Mono.just(ResponseEntity.ok(
                            new UOResponse("success", message, null, 
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    } else {
                        return Mono.just(ResponseEntity.badRequest().body(
                            new UOResponse("error", message, null, 
                                List.of(new ServiceStatus("i-pay", false, message)))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse SOAP", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new UOResponse("error", "Erreur technique", null, 
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new UOResponse("error", "Service indisponible", null, 
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
                ));
            });
    }

    @GetMapping("/history")
public Mono<ResponseEntity<HistoryResponse>> getHistorySolde(@RequestHeader(value = "Authorization", required = false) String authHeader) {
    // Vérifications du token (comme pour les autres endpoints)
    if (authHeader == null || authHeader.isBlank()) {
        return buildUnauthorizedResponse(
            new HistoryResponse("error", "Token manquant", null, 
                List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
    }

    if (!authHeader.startsWith("Bearer ")) {
        return buildUnauthorizedResponse(
            new HistoryResponse("error", "Format token invalide", null,
                List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
    }

    String jwt = authHeader.substring(7);
    if (!jwtUtil.validateToken(jwt)) {
        return buildUnauthorizedResponse(
            new HistoryResponse("error", "Token invalide/expiré", null,
                List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
    }

    String ipayToken = jwtUtil.extractIpayToken(jwt);
    String userId = jwtUtil.extractUserId(jwt);
    
    if (ipayToken == null || userId == null) {
        return buildUnauthorizedResponse(
            new HistoryResponse("error", "Token incomplet", null,
                List.of(new ServiceStatus("i-pay", false, "Non autorisé"))));
    }

    return ipayService.getHistorySolde(ipayToken, userId)
        .flatMap(xmlResponse -> {
            try {
                Document doc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder()
                        .parse(new InputSource(new StringReader(xmlResponse)));
                
                XPath xpath = XPathFactory.newInstance().newXPath();
                String error = xpath.evaluate("//return/error", doc);
                String message = xpath.evaluate("//return/message", doc);

                if ("0".equals(error)) {
                    List<HistoryItem> historyItems = new ArrayList<>();
                    
                    // Si le message indique "Aucune opération"
                    if ("Aucune opération".equals(message)) {
                        return Mono.just(ResponseEntity.ok(
                            new HistoryResponse("success", message, historyItems,
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    }
                    
                    // Sinon, traitement normal de l'historique
                    // (à adapter selon la structure exacte de la réponse)
                    // NodeList items = (NodeList) xpath.evaluate("//return/history/item", doc, XPathConstants.NODESET);
                    // for(int i = 0; i < items.getLength(); i++) {
                    //     Node item = items.item(i);
                    //     historyItems.add(new HistoryItem(
                    //         xpath.evaluate("date", item),
                    //         xpath.evaluate("montant", item),
                    //         xpath.evaluate("operation", item)
                    //     ));
                    // }
                    
                    return Mono.just(ResponseEntity.ok(
                        new HistoryResponse("success", message, historyItems,
                            List.of(new ServiceStatus("i-pay", true, "Historique récupéré")))
                    ));
                } else {
                    return Mono.just(ResponseEntity.badRequest().body(
                        new HistoryResponse("error", message, null,
                            List.of(new ServiceStatus("i-pay", false, message)))
                    ));
                }
            } catch (Exception e) {
                return Mono.just(ResponseEntity.internalServerError().body(
                    new HistoryResponse("error", "Erreur technique", null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                ));
            }
        })
        .onErrorResume(e -> {
            return Mono.just(ResponseEntity.internalServerError().body(
                new HistoryResponse("error", "Service indisponible", null,
                    List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
            ));
        });
}



    private Mono<ResponseEntity<SoldeResponse>> handleSoapResponse(String xmlResponse) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlResponse)));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            String error = xpath.evaluate("//return/error", doc);
            String message = xpath.evaluate("//return/message", doc);
            String montant = xpath.evaluate("//return/montant", doc);

            if ("0".equals(error)) {
                logger.info("Solde récupéré avec succès: {}", montant);
                return Mono.just(ResponseEntity.ok(
                    new SoldeResponse(
                        "success",
                        message,
                        montant,
                        List.of(new ServiceStatus("i-pay", true, "Solde récupéré"))
                )));
            } else {
                logger.warn("Erreur IPay: {}", message);
                return Mono.just(ResponseEntity.badRequest().body(
                    new SoldeResponse(
                        "error",
                        message,
                        "0.00",
                        List.of(new ServiceStatus("i-pay", false, message)))
                ));
            }
        } catch (Exception e) {
            logger.error("Erreur de traitement XML", e);
            return buildErrorResponse("Erreur de traitement de la réponse");
        }
    }

    private Mono<ResponseEntity<SoldeResponse>> handleError(Throwable e) {
        logger.error("Erreur lors de l'appel IPay", e);
        return buildErrorResponse("Erreur du service IPay");
    }

    private <T> Mono<ResponseEntity<T>> buildUnauthorizedResponse(T response) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
    }

    private Mono<ResponseEntity<SoldeResponse>> buildErrorResponse(String message) {
        return Mono.just(ResponseEntity.internalServerError().body(
            new SoldeResponse(
                "error",
                message,
                "0.00",
                List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
            )
        ));
    }
}
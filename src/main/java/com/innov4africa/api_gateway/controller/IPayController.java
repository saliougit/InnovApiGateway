package com.innov4africa.api_gateway.controller;

import com.innov4africa.api_gateway.model.HistoryItem;
import com.innov4africa.api_gateway.model.HistoryResponse;
import com.innov4africa.api_gateway.model.LogoutResponse;
import com.innov4africa.api_gateway.model.Notification;
import com.innov4africa.api_gateway.model.NotificationResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.model.SoldeResponse;
import com.innov4africa.api_gateway.model.Transaction;
import com.innov4africa.api_gateway.model.TransactionResponse;
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
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

    /**
     * Endpoint pour déconnecter un utilisateur (logout)
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse indiquant le succès ou l'échec de la déconnexion
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<LogoutResponse>> deconnexionUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de déconnexion sans header Authorization");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token d'authentification manquant",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide lors de la déconnexion: {}", authHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Format de token invalide",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré lors de la déconnexion");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token invalide ou expiré",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        // 4. Extraction du token IPay
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (ipayToken == null) {
            logger.warn("Token ne contient pas le token IPay");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token incomplet",
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            ));
        }

        logger.info("Demande de déconnexion avec le token IPay: {}", ipayToken);
        
        // 5. Appel du service IPay pour la déconnexion
        return ipayService.deconnexionUser(ipayToken)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String code = xpath.evaluate("//return/code", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("1".equals(code)) {
                        logger.info("Déconnexion réussie");
                        return Mono.just(ResponseEntity.ok(
                            new LogoutResponse("success", "Déconnexion réussie", 
                                List.of(new ServiceStatus("i-pay", true, message)))
                        ));
                    } else {
                        logger.warn("Échec de déconnexion: {} - {}", code, message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new LogoutResponse("error", message,
                                List.of(new ServiceStatus("i-pay", false, "Échec de déconnexion")))
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement de la réponse de déconnexion", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new LogoutResponse("error", "Erreur technique",
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement")))
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service de déconnexion", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new LogoutResponse("error", "Service indisponible",
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication")))
                ));
            });
    }

    /**
     * Endpoint pour récupérer les transactions d'un compte
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse contenant la liste des transactions et leur nombre total
     */
    @GetMapping("/operations")
    public Mono<ResponseEntity<TransactionResponse>> getOperations(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès aux opérations sans header Authorization");
            return buildUnauthorizedResponse(
                new TransactionResponse("error", "Token d'authentification manquant", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour les opérations: {}", authHeader);
            return buildUnauthorizedResponse(
                new TransactionResponse("error", "Format de token invalide", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour les opérations");
            return buildUnauthorizedResponse(
                new TransactionResponse("error", "Token invalide ou expiré", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        // String userId = jwtUtil.extractUserId(jwt);
        String userId = "100";
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (userId == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - userId: {}, ipayToken: {}", userId, ipayToken);
            return buildUnauthorizedResponse(
                new TransactionResponse("error", "Token incomplet", 0, null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande des opérations pour l'utilisateur ID: {}", userId);
        
        // 5. Appel du service IPay
        return ipayService.getOperationCompte(ipayToken, userId)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);
                    String totalStr = xpath.evaluate("//return/total", doc);
                    
                    Integer total = totalStr != null && !totalStr.isEmpty() ? Integer.parseInt(totalStr) : 0;

                    if ("0".equals(error)) {
                        List<Transaction> transactions = new ArrayList<>();
                        
                        // Si des opérations existent dans la réponse (dans le cas où total > 0)
                        if (total > 0) {
                            try {
                                NodeList operations = (NodeList) xpath.evaluate("//return/operations/item", doc, XPathConstants.NODESET);
                                for (int i = 0; i < operations.getLength(); i++) {
                                    String date = xpath.evaluate("date", operations.item(i));
                                    String montant = xpath.evaluate("montant", operations.item(i));
                                    String type = xpath.evaluate("type", operations.item(i));
                                    String description = xpath.evaluate("description", operations.item(i));
                                    String reference = xpath.evaluate("reference", operations.item(i));
                                    
                                    transactions.add(new Transaction(date, montant, type, description, reference));
                                }
                            } catch (Exception e) {
                                logger.warn("Erreur lors du parsing des opérations: {}", e.getMessage());
                            }
                        }
                        
                        return Mono.just(ResponseEntity.ok(
                            new TransactionResponse(
                                "success", 
                                message, 
                                total, 
                                transactions, 
                                List.of(new ServiceStatus("i-pay", true, "Opérations récupérées"))
                            )
                        ));
                    } else {
                        logger.warn("Erreur IPay lors de la récupération des opérations: {}", message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new TransactionResponse(
                                "error",
                                message,
                                0,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour les opérations", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new TransactionResponse(
                            "error",
                            "Erreur technique",
                            0,
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour les opérations", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new TransactionResponse(
                        "error",
                        "Service indisponible",
                        0,
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
                ));
            });
    }

    /**
     * Endpoint pour récupérer les notifications d'un utilisateur
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse contenant la liste des notifications
     */
    @GetMapping("/notifications")
    public Mono<ResponseEntity<NotificationResponse>> getNotifications(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès aux notifications sans header Authorization");
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token d'authentification manquant", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide pour les notifications: {}", authHeader);
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Format de token invalide", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré pour les notifications");
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token invalide ou expiré", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        // 4. Extraction des claims
        String userId = jwtUtil.extractUserId(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (userId == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - userId: {}, ipayToken: {}", userId, ipayToken);
            return buildUnauthorizedResponse(
                new NotificationResponse("error", "Token incomplet", null,
                    List.of(new ServiceStatus("i-pay", false, "Non autorisé")))
            );
        }

        logger.info("Demande des notifications pour l'utilisateur ID: {}", userId);
        
        // 5. Appel du service IPay
        return ipayService.getAllNotif(ipayToken, userId)
            .flatMap(xmlResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(xmlResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String error = xpath.evaluate("//return/error", doc);
                    String message = xpath.evaluate("//return/message", doc);

                    if ("0".equals(error)) {
                        List<Notification> notifications = new ArrayList<>();
                        
                        // Si le message n'indique pas "Aucune notification", tenter de récupérer les notifications
                        if (!"Aucune notification".equals(message)) {
                            try {
                                NodeList notifNodes = (NodeList) xpath.evaluate("//return/notifications/item", doc, XPathConstants.NODESET);
                                for (int i = 0; i < notifNodes.getLength(); i++) {
                                    String id = xpath.evaluate("id", notifNodes.item(i));
                                    String date = xpath.evaluate("date", notifNodes.item(i));
                                    String notifMessage = xpath.evaluate("message", notifNodes.item(i));
                                    String type = xpath.evaluate("type", notifNodes.item(i));
                                    String status = xpath.evaluate("status", notifNodes.item(i));
                                    
                                    notifications.add(new Notification(id, date, notifMessage, type, status));
                                }
                            } catch (Exception e) {
                                logger.warn("Erreur lors du parsing des notifications: {}", e.getMessage());
                            }
                        }
                        
                        return Mono.just(ResponseEntity.ok(
                            new NotificationResponse(
                                "success", 
                                message, 
                                notifications, 
                                List.of(new ServiceStatus("i-pay", true, "Notifications récupérées"))
                            )
                        ));
                    } else {
                        logger.warn("Erreur IPay lors de la récupération des notifications: {}", message);
                        return Mono.just(ResponseEntity.badRequest().body(
                            new NotificationResponse(
                                "error",
                                message,
                                null,
                                List.of(new ServiceStatus("i-pay", false, message))
                            )
                        ));
                    }
                } catch (Exception e) {
                    logger.error("Erreur de traitement de la réponse XML pour les notifications", e);
                    return Mono.just(ResponseEntity.internalServerError().body(
                        new NotificationResponse(
                            "error",
                            "Erreur technique",
                            null,
                            List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
                        )
                    ));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur lors de l'appel au service IPay pour les notifications", e);
                return Mono.just(ResponseEntity.internalServerError().body(
                    new NotificationResponse(
                        "error",
                        "Service indisponible",
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Erreur de communication"))
                    )
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
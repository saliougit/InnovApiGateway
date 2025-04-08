package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.model.AuthResult;
import com.innov4africa.api_gateway.model.LogoutResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.repository.TokenRepository;
import com.innov4africa.api_gateway.repository.UserSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.time.Duration;


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private IPayService ipayService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private TokenRepository tokenRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;


        /**
     * Force une déconnexion puis reconnexion complète pour récupérer toutes les informations utilisateur
     * @param existingToken Token IPay existant
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return Réponse d'authentification avec toutes les informations récupérées
     */
    private Mono<AuthResponse> forceDisconnectAndReconnect(String existingToken, String email, String password) {
        // Utilise la méthode de déconnexion existante dans IPayService
        return ipayService.deconnexionUser(existingToken)
            .flatMap(deconnectResponse -> {
                try {
                    Document doc = DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .parse(new InputSource(new StringReader(deconnectResponse)));
                    
                    XPath xpath = XPathFactory.newInstance().newXPath();
                    String code = xpath.evaluate("//return/code", doc);
                    
                    logger.info("Résultat de la déconnexion forcée pour {}: code={}", email, code);
                    
                    // Que la déconnexion réussisse ou échoue, attendre un court délai puis tenter une reconnexion complète
                    // Le délai permet au serveur IPay de terminer complètement la session précédente
                    return Mono.delay(Duration.ofMillis(500))
                        .then(ipayService.authenticate(email, password));
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement de la réponse de déconnexion", e);
                    // Attendre un court délai avant de tenter la reconnexion
                    return Mono.delay(Duration.ofMillis(500))
                        .then(ipayService.authenticate(email, password));
                }
            })
            .map(newAuthResult -> {
                if (newAuthResult.isSuccess()) {
                    logger.info("Reconnexion réussie pour: {}", email);
                    logger.info("Nouveau token IPay: {}", newAuthResult.getToken());
                    logger.info("Téléphone après reconnexion: {}", newAuthResult.getTelephone());
                    logger.info("User ID après reconnexion: {}", newAuthResult.getIduser());
                    
                    // Sauvegarder les nouvelles informations de session
                    if (newAuthResult.getToken() != null) {
                        userSessionRepository.saveUserSession(
                            newAuthResult.getToken(), 
                            newAuthResult.getIduser(), 
                            newAuthResult.getTelephone()
                        );
                    }
                    
                    return buildSuccessResponse(newAuthResult, email);
                } else {
                    logger.warn("Échec de la reconnexion pour: {}: {}", email, newAuthResult.getMessage());
                    return buildErrorResponse(newAuthResult.getMessage());
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors de la reconnexion pour: {}", email, e);
                return Mono.just(buildErrorResponse("Erreur technique lors de la reconnexion: " + e.getMessage()));
            });
    }


    // public Mono<AuthResponse> authenticate(AuthRequest request) {
    //     String email = request.getEmail();
    //     String password = request.getPassword();
    
    //     return ipayService.authenticate(email, password)
    //         .map(authResult -> {
    //             if (authResult.isSuccess()) {
    //                 // Afficher le token IPay dans les logs
    //                 logger.info("Token IPay reçu: {}", authResult.getToken());
                    
    //                 String ipayToken = authResult.getToken();
    //                 String telephone = authResult.getTelephone();
    //                 String userId = authResult.getIduser();
                    
    //                 // Vérifier si c'est un cas de "session déjà en cours" (error=13)
    //                 if (authResult.getMessage() != null && 
    //                     authResult.getMessage().contains("session en cours")) {
                        
    //                     logger.info("Session déjà en cours détectée pour: {}", email);
                        
    //                     // Si le téléphone ou l'userId est manquant, essayer de les récupérer du repository
    //                     if ((telephone == null || userId == null) && userSessionRepository.hasSessionInfo(ipayToken)) {
    //                         UserSessionRepository.UserSessionInfo sessionInfo = userSessionRepository.getUserSessionInfo(ipayToken);
                            
    //                         if (sessionInfo != null) {
    //                             if (telephone == null) {
    //                                 telephone = sessionInfo.getTelephone();
    //                                 logger.info("Téléphone récupéré du repository: {}", telephone);
    //                             }
                                
    //                             if (userId == null) {
    //                                 userId = sessionInfo.getUserId();
    //                                 logger.info("UserId récupéré du repository: {}", userId);
    //                             }
    //                         }
    //                     }
    //                 }
                    
    //                 // Si nous avons récupéré les informations complètes, les sauvegarder dans le repository
    //                 if (ipayToken != null && (userId != null || telephone != null)) {
    //                     userSessionRepository.saveUserSession(ipayToken, userId, telephone);
    //                 }
                    
    //                 logger.info("Téléphone IPay: {}", telephone);
    //                 logger.info("User ID: {}", userId);
                    
    //                 // Génère le JWT avec toutes les infos IPay
    //                 String jwtToken = jwtUtil.generateIpayToken(
    //                     email,
    //                     ipayToken,
    //                     telephone,
    //                     userId
    //                 );
                    
    //                 return new AuthResponse(
    //                     "success",
    //                     authResult.getMessage(),
    //                     jwtToken,
    //                     List.of(new ServiceStatus("i-pay", true, authResult.getMessage()))
    //                 );
    //             } else {
    //                 return new AuthResponse(
    //                     "error",
    //                     authResult.getMessage(),
    //                     null,
    //                     List.of(new ServiceStatus("i-pay", false, authResult.getMessage()))
    //                 );
    //             }
    //         })
    //         .onErrorResume(e -> Mono.just(
    //             new AuthResponse(
    //                 "error",
    //                 "Erreur technique: " + e.getMessage(),
    //                 null,
    //                 List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
    //             )
    //         ));
    // }
    

    // // public Mono<AuthResponse> authenticate(AuthRequest request) {
    //     String email = request.getEmail();
    //     String password = request.getPassword();
    
    //     return ipayService.authenticate(email, password)
    //         .map(authResult -> {
    //             if (authResult.isSuccess()) {

    //                 // Afficher le token IPay dans les logs
    //                 logger.info("Token IPay reçu: {}", authResult.getToken());
    //                 logger.info("Téléphone IPay: {}", authResult.getTelephone());
    //                 logger.info("User ID: {}", authResult.getIduser());
                    
    //                 // Génère le JWT avec toutes les infos IPay
    //                 String jwtToken = jwtUtil.generateIpayToken(
    //                     email,
    //                     authResult.getToken(), // token IPay
    //                     authResult.getTelephone(),
    //                     authResult.getIduser()
    //                 );
                    
    //                 return new AuthResponse(
    //                     "success",
    //                     authResult.getMessage(),
    //                     jwtToken,
    //                     List.of(new ServiceStatus("i-pay", true, authResult.getMessage()))
    //                 );
    //             } else {
    //                 return new AuthResponse(
    //                     "error",
    //                     authResult.getMessage(),
    //                     null,
    //                     List.of(new ServiceStatus("i-pay", false, authResult.getMessage()))
    //                 );
    //             }
    //         })
    //         .onErrorResume(e -> Mono.just(
    //             new AuthResponse(
    //                 "error",
    //                 "Erreur technique: " + e.getMessage(),
    //                 null,
    //                 List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
    //             )
    //         ));
    // }
    
    /**
     * Méthode de déconnexion globale qui gère la déconnexion de tous les services intégrés
     * @param jwt Token JWT de l'utilisateur
     * @return Réponse indiquant le succès ou l'échec de la déconnexion pour chaque service
     */
    public Mono<LogoutResponse> logout(String jwt) {
        logger.info("Demande de déconnexion globale");
        
        // Valider le JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Tentative de déconnexion avec un token invalide");
            return Mono.just(new LogoutResponse(
                "error", 
                "Token invalide ou expiré",
                List.of(new ServiceStatus("global", false, "Non autorisé"))
            ));
        }
        
        // Ajouter le token à la liste des tokens révoqués (si le TokenRepository est disponible)
        if (tokenRepository != null) {
            tokenRepository.saveRevokedToken(jwt, jwtUtil.getExpirationDateFromToken(jwt));
        }
        
        // Liste pour suivre l'état de déconnexion de chaque service
        List<ServiceStatus> serviceStatuses = new ArrayList<>();
        
        // Récupération des tokens des différents services depuis le JWT
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        // Déconnexion de IPay si un token est disponible
        if (ipayToken != null) {
            return ipayService.deconnexionUser(ipayToken)
                .flatMap(xmlResponse -> {
                    try {
                        Document doc = DocumentBuilderFactory.newInstance()
                                .newDocumentBuilder()
                                .parse(new InputSource(new StringReader(xmlResponse)));
                        
                        XPath xpath = XPathFactory.newInstance().newXPath();
                        String code = xpath.evaluate("//return/code", doc);
                        String message = xpath.evaluate("//return/message", doc);

                        // Ajouter le statut de déconnexion de IPay
                        if ("1".equals(code)) {
                            serviceStatuses.add(new ServiceStatus("i-pay", true, message));
                        } else {
                            serviceStatuses.add(new ServiceStatus("i-pay", false, message));
                        }
                        
                        // Dans le futur, ajouter ici d'autres services comme i-banking
                        // serviceStatuses.add(new ServiceStatus("i-banking", true, "Déconnecté"));
                        
                        // Si au moins un service a réussi à se déconnecter, considérer que la déconnexion est réussie
                        boolean anySuccess = serviceStatuses.stream().anyMatch(ServiceStatus::isAvailable);
                        
                        return Mono.just(new LogoutResponse(
                            anySuccess ? "success" : "partial", 
                            anySuccess ? "Déconnexion globale réussie" : "Déconnexion partielle",
                            serviceStatuses
                        ));
                        
                    } catch (Exception e) {
                        logger.error("Erreur lors du traitement de la réponse de déconnexion IPay", e);
                        serviceStatuses.add(new ServiceStatus("i-pay", false, "Erreur technique"));
                        return Mono.just(new LogoutResponse("error", "Erreur technique", serviceStatuses));
                    }
                })
                .onErrorResume(e -> {
                    logger.error("Erreur lors de la déconnexion IPay", e);
                    serviceStatuses.add(new ServiceStatus("i-pay", false, "Service indisponible"));
                    return Mono.just(new LogoutResponse("error", "Service indisponible", serviceStatuses));
                });
        } else {
            serviceStatuses.add(new ServiceStatus("i-pay", false, "Token IPay non disponible"));
            return Mono.just(new LogoutResponse("error", "Token incomplet", serviceStatuses));
        }
    }




    // public Mono<AuthResponse> authenticate(AuthRequest request) {
    //     String email = request.getEmail();
    //     String password = request.getPassword();
    
    //     return ipayService.authenticate(email, password)
    //         .flatMap(authResult -> {
    //             if (authResult.isSuccess()) {
    //                 // Traitement normal pour une authentification réussie sans session en cours
    //                 logger.info("Authentification réussie pour: {}", email);
    //                 logger.info("Token IPay reçu: {}", authResult.getToken());
                    
    //                 String ipayToken = authResult.getToken();
    //                 String telephone = authResult.getTelephone();
    //                 String userId = authResult.getIduser();
                    
    //                 // Si nous avons récupéré les informations complètes, les sauvegarder dans le repository
    //                 if (ipayToken != null && (userId != null || telephone != null)) {
    //                     userSessionRepository.saveUserSession(ipayToken, userId, telephone);
    //                 }
                    
    //                 logger.info("Téléphone IPay: {}", telephone);
    //                 logger.info("User ID: {}", userId);
                    
    //                 return Mono.just(buildSuccessResponse(authResult, email));
                    
    //             } else if (
    //                      authResult.getMessage().contains("session en cours")) {
                    
    //                 logger.info("Session déjà en cours détectée pour: {}, déconnexion et reconnexion complète...", email);
                    
    //                 // Récupérer le token de la session en cours
    //                 String existingToken = authResult.getToken();

    //                 //debug du token passe
    //                 logger.info("Token existant: {}", existingToken);
                    
    //                 // Force une déconnexion complète puis reconnexion
    //                 return forceDisconnectAndReconnect(existingToken, email, password);
    //             } else {
    //                 // Autre type d'erreur
    //                 logger.warn("Erreur d'authentification pour: {}: {}", email, authResult.getMessage());
    //                 return Mono.just(buildErrorResponse(authResult.getMessage()));
    //             }
    //         })
    //         .onErrorResume(e -> {
    //             logger.error("Erreur technique lors de l'authentification pour: {}", email, e);
    //             return Mono.just(buildErrorResponse("Erreur technique: " + e.getMessage()));
    //         });
    // }

        public Mono<AuthResponse> authenticate(AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
    
        return ipayService.authenticate(email, password)
            .flatMap(authResult -> {
                // Afficher tous les détails de l'authentification pour le débogage
                logger.info("Résultat d'authentification: success={}, message={}, token={}, telephone={}, userId={}", 
                         authResult.isSuccess(), 
                         authResult.getMessage(), 
                         authResult.getToken(), 
                         authResult.getTelephone(), 
                         authResult.getIduser());
                
                // Vérifier spécifiquement si nous avons une session en cours (code d'erreur 13)
                boolean isSessionEnCours = authResult.getMessage() != null && 
                                          authResult.getMessage().contains("session en cours") &&
                                          authResult.getToken() != null;
                
                // Si une session est déjà en cours, forcer une déconnexion puis reconnexion
                if (isSessionEnCours) {
                    logger.info("Session déjà en cours détectée pour: {}, déconnexion et reconnexion complète...", email);
                    
                    // Récupérer le token de la session en cours
                    String existingToken = authResult.getToken();
    
                    // Debug du token passé à la méthode de déconnexion
                    logger.info("Tentative de déconnexion avec le token existant: {}", existingToken);
                    
                    // Force une déconnexion complète puis reconnexion
                    return forceDisconnectAndReconnect(existingToken, email, password);
                } 
                // Sinon, traitement normal pour une authentification réussie
                else if (authResult.isSuccess()) {
                    logger.info("Authentification réussie pour: {}", email);
                    logger.info("Token IPay reçu: {}", authResult.getToken());
                    
                    String ipayToken = authResult.getToken();
                    String telephone = authResult.getTelephone();
                    String userId = authResult.getIduser();
                    
                    // Si nous avons récupéré les informations complètes, les sauvegarder dans le repository
                    if (ipayToken != null && (userId != null || telephone != null)) {
                        userSessionRepository.saveUserSession(ipayToken, userId, telephone);
                    }
                    
                    logger.info("Téléphone IPay: {}", telephone);
                    logger.info("User ID: {}", userId);
                    
                    return Mono.just(buildSuccessResponse(authResult, email));
                } 
                // Autre type d'erreur
                else {
                    logger.warn("Erreur d'authentification pour: {}: {}", email, authResult.getMessage());
                    return Mono.just(buildErrorResponse(authResult.getMessage()));
                }
            })
            .onErrorResume(e -> {
                logger.error("Erreur technique lors de l'authentification pour: {}", email, e);
                return Mono.just(buildErrorResponse("Erreur technique: " + e.getMessage()));
            });
    }

//     // Dans AuthService.java - Remplacer la méthode authenticate

// public Mono<AuthResponse> authenticate(AuthRequest request) {
//     String email = request.getEmail();
//     String password = request.getPassword();

//     return ipayService.authenticate(email, password)
//         .flatMap(authResult -> {
//             if (authResult.isSuccess()) {
//                 // Traitement normal pour une authentification réussie
//                 logger.info("Authentification réussie pour: {}", email);
//                 logger.info("Token IPay reçu: {}", authResult.getToken());
                
//                 String ipayToken = authResult.getToken();
//                 String telephone = authResult.getTelephone();
//                 String userId = authResult.getIduser();
                
//                 // Si nous avons récupéré les informations complètes, les sauvegarder dans le repository
//                 if (ipayToken != null && (userId != null || telephone != null)) {
//                     userSessionRepository.saveUserSession(ipayToken, userId, telephone);
//                 }
                
//                 logger.info("Téléphone IPay: {}", telephone);
//                 logger.info("User ID: {}", userId);
                
//                 return Mono.just(buildSuccessResponse(authResult, email));
                
//             } else if (authResult.getMessage() != null && 
//                      authResult.getMessage().contains("session en cours") && 
//                      authResult.getToken() != null) {
                
//                 logger.info("Session déjà en cours détectée pour: {}, déconnexion et reconnexion...", email);
                
//                 // Récupérer le token de la session en cours
//                 String existingToken = authResult.getToken();
                
//                 // Déconnexion puis reconnexion
//                 return ipayService.deconnexionUser(existingToken)
//                     .flatMap(deconnectResponse -> {
//                         try {
//                             Document doc = DocumentBuilderFactory.newInstance()
//                                     .newDocumentBuilder()
//                                     .parse(new InputSource(new StringReader(deconnectResponse)));
                            
//                             XPath xpath = XPathFactory.newInstance().newXPath();
//                             String code = xpath.evaluate("//return/code", doc);
                            
//                             if ("1".equals(code)) {
//                                 logger.info("Déconnexion réussie, reconnexion en cours pour: {}", email);
//                                 // Reconnexion avec les mêmes identifiants
//                                 return ipayService.authenticate(email, password);
//                             } else {
//                                 logger.warn("Échec de la déconnexion pour: {}, tentative de reconnexion quand même", email);
//                                 return ipayService.authenticate(email, password);
//                             }
//                         } catch (Exception e) {
//                             logger.error("Erreur lors du traitement de la réponse de déconnexion", e);
//                             // Tenter la reconnexion même en cas d'erreur de traitement
//                             return ipayService.authenticate(email, password);
//                         }
//                     })
//                     .map(newAuthResult -> {
//                         if (newAuthResult.isSuccess()) {
//                             logger.info("Reconnexion réussie pour: {}", email);
//                             return buildSuccessResponse(newAuthResult, email);
//                         } else {
//                             logger.warn("Échec de la reconnexion pour: {}: {}", email, newAuthResult.getMessage());
//                             return buildErrorResponse(newAuthResult.getMessage());
//                         }
//                     })
//                     .onErrorResume(e -> {
//                         logger.error("Erreur technique lors de la déconnexion/reconnexion pour: {}", email, e);
//                         return Mono.just(buildErrorResponse("Erreur technique lors de la déconnexion/reconnexion: " + e.getMessage()));
//                     });
//             } else {
//                 // Autre type d'erreur
//                 logger.warn("Erreur d'authentification pour: {}: {}", email, authResult.getMessage());
//                 return Mono.just(buildErrorResponse(authResult.getMessage()));
//             }
//         })
//         .onErrorResume(e -> {
//             logger.error("Erreur technique lors de l'authentification pour: {}", email, e);
//             return Mono.just(buildErrorResponse("Erreur technique: " + e.getMessage()));
//         });
// }

// Ajouter ces méthodes utilitaires si elles n'existent pas déjà

private AuthResponse buildSuccessResponse(AuthResult authResult, String email) {
    String jwtToken = jwtUtil.generateIpayToken(
        email,
        authResult.getToken(),
        authResult.getTelephone(),
        authResult.getIduser()
    );
    
    return new AuthResponse(
        "success",
        authResult.getMessage(),
        jwtToken,
        List.of(new ServiceStatus("i-pay", true, authResult.getMessage()))
    );
}

private AuthResponse buildErrorResponse(String message) {
    return new AuthResponse(
        "error",
        message,
        null,
        List.of(new ServiceStatus("i-pay", false, message))
    );
}
}

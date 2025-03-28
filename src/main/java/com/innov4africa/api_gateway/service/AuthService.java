package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.model.LogoutResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.repository.TokenRepository;

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


@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private IPayService ipayService;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired(required = false)
    private TokenRepository tokenRepository;

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();
    
        return ipayService.authenticate(email, password)
            .map(authResult -> {
                if (authResult.isSuccess()) {

                    // Afficher le token IPay dans les logs
                    logger.info("Token IPay reçu: {}", authResult.getToken());
                    logger.info("Téléphone IPay: {}", authResult.getTelephone());
                    logger.info("User ID: {}", authResult.getIduser());
                    
                    // Génère le JWT avec toutes les infos IPay
                    String jwtToken = jwtUtil.generateIpayToken(
                        email,
                        authResult.getToken(), // token IPay
                        authResult.getTelephone(),
                        authResult.getIduser()
                    );
                    
                    return new AuthResponse(
                        "success",
                        authResult.getMessage(),
                        jwtToken,
                        List.of(new ServiceStatus("i-pay", true, authResult.getMessage()))
                    );
                } else {
                    return new AuthResponse(
                        "error",
                        authResult.getMessage(),
                        null,
                        List.of(new ServiceStatus("i-pay", false, authResult.getMessage()))
                    );
                }
            })
            .onErrorResume(e -> Mono.just(
                new AuthResponse(
                    "error",
                    "Erreur technique: " + e.getMessage(),
                    null,
                    List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
                )
            ));
    }
    
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
}

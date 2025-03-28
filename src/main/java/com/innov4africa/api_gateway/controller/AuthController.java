package com.innov4africa.api_gateway.controller;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.model.LogoutResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return authService.authenticate(request)
            .map(response -> {
                if ("success".equals(response.getStatus())) {
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(401).body(response);
                }
            });
    }
    
    /**
     * Endpoint pour la déconnexion globale de l'utilisateur de tous les services
     * @param authHeader Le header d'autorisation contenant le JWT
     * @return Une réponse indiquant le succès ou l'échec de la déconnexion globale
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<LogoutResponse>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative de déconnexion sans header Authorization");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Token d'authentification manquant",
                    List.of(new ServiceStatus("global", false, "Non autorisé")))
            ));
        }

        // Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide lors de la déconnexion: {}", authHeader);
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new LogoutResponse("error", "Format de token invalide",
                    List.of(new ServiceStatus("global", false, "Non autorisé")))
            ));
        }

        String jwt = authHeader.substring(7);
        logger.info("Demande de déconnexion globale reçue");
        
        // Appel du service pour la déconnexion globale
        return authService.logout(jwt)
            .map(response -> {
                if ("success".equals(response.getStatus())) {
                    return ResponseEntity.ok(response);
                } else if ("partial".equals(response.getStatus())) {
                    return ResponseEntity.ok(response); // Considérer succès partiel comme un succès HTTP 200
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            });
    }
}

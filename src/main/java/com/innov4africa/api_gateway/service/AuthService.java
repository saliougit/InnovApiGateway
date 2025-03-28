package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.model.ServiceStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    private IPayService ipayService;

    @Autowired
    private JwtUtil jwtUtil;

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
}



// @Service
// public class AuthService {

//     @Autowired
//     private IPayService ipayService;

//     @Autowired
//     private JwtUtil jwtUtil;

//     public Mono<AuthResponse> authenticate(AuthRequest request) {
//         String email = request.getEmail();
//         String password = request.getPassword();
    
//         return ipayService.authenticate(email, password)
//             .map(authResult -> {
//                 if (authResult.isSuccess()) {
//                     String jwtToken = jwtUtil.generateToken(email);
//                     return new AuthResponse(
//                         "success",
//                         authResult.getMessage(),
//                         jwtToken,
//                         List.of(new ServiceStatus("i-pay", true, authResult.getMessage()))
//                     );
//                 } else {
//                     return new AuthResponse(
//                         "error",
//                         authResult.getMessage(),
//                         null,
//                         List.of(new ServiceStatus("i-pay", false, authResult.getMessage()))
//                     );
//                 }
//             })
//             .onErrorResume(e -> Mono.just(
//                 new AuthResponse(
//                     "error",
//                     "Erreur technique: " + e.getMessage(),
//                     null,
//                     List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
//                 )
//             ));
//     }

//     // public Mono<AuthResponse> authenticate(AuthRequest request) {
//     //     String email = request.getEmail();
//     //     String password = request.getPassword();

//     //     // Authentification avec i-pay
//     //     return ipayService.authenticate(email, password).flatMap(authResult -> {
//     //         if (authResult.isSuccess()) {
//     //             // Générer un token JWT
//     //             String jwtToken = jwtUtil.generateToken(email);
//     //             return Mono.just(new AuthResponse(
//     //                     "success",
//     //                     authResult.getMessage(), // Message de succès
//     //                     jwtToken,
//     //                     List.of(new ServiceStatus("i-pay", true, "Service disponible"))
//     //             ));
//     //         } else {
//     //             return Mono.just(new AuthResponse(
//     //                     "error",
//     //                     authResult.getMessage(), // Message d'erreur
//     //                     null,
//     //                     List.of(new ServiceStatus("i-pay", false, "Service indisponible"))
//     //             ));
//     //         }
//     //     });
//     // }
// }

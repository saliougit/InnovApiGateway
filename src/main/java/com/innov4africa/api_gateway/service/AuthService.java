package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.model.AuthResult;
import com.innov4africa.api_gateway.model.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private IPayService ipayService;

    @Autowired
    private JwtUtil jwtUtil;

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Authentification avec i-pay
        return ipayService.authenticate(email, password).flatMap(authResult -> {
            if (authResult.isSuccess()) {
                // Générer un token JWT
                String jwtToken = jwtUtil.generateToken(email);
                return Mono.just(new AuthResponse(
                        "success",
                        authResult.getMessage(), // Message de succès
                        jwtToken,
                        List.of(new ServiceStatus("i-pay", true, "Service disponible"))
                ));
            } else {
                return Mono.just(new AuthResponse(
                        "error",
                        authResult.getMessage(), // Message d'erreur
                        null,
                        List.of(new ServiceStatus("i-pay", false, "Service indisponible"))
                ));
            }
        });
    }
}



// package com.innov4africa.api_gateway.service;

// import com.innov4africa.api_gateway.model.AuthRequest;
// import com.innov4africa.api_gateway.model.AuthResponse;
// import com.innov4africa.api_gateway.model.ServiceStatus;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import reactor.core.publisher.Mono;

// import java.util.List;

// @Service
// public class AuthService {

//     @Autowired
//     private IPayService ipayService;

//     @Autowired
//     private JwtUtil jwtUtil;

//     public Mono<AuthResponse> authenticate(AuthRequest request) {
//         String email = request.getEmail();
//         String password = request.getPassword();

//         // Authentification avec i-pay
//         return ipayService.authenticate(email, password).flatMap(ipaySuccess -> {
//             if (ipaySuccess) {
//                 // Générer un token JWT
//                 String jwtToken = jwtUtil.generateToken(email);
//                 return Mono.just(new AuthResponse(
//                         "success",
//                         "Authentification réussie",
//                         jwtToken,
//                         List.of(new ServiceStatus("i-pay", true, "Service disponible"))
//                 ));
//             } else {
//                 return Mono.just(new AuthResponse(
//                         "error",
//                         "Authentification i-pay échouée",
//                         null,
//                         List.of(new ServiceStatus("i-pay", false, "Service indisponible"))
//                 ));
//             }
//         });
//     }
// }


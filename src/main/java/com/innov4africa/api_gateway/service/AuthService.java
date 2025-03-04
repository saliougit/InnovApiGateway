package com.innov4africa.api_gateway.service;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
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
    private IBankingService ibankingService;

    @Autowired
    private JwtUtil jwtUtil;

    public Mono<AuthResponse> authenticate(AuthRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        // Authentification parallèle avec i-pay et i-banking
        Mono<Boolean> ipayAuth = ipayService.authenticate(email, password);
        Mono<Boolean> ibankingAuth = Mono.just(ibankingService.authenticate(email, password));

        return Mono.zip(ipayAuth, ibankingAuth).map(results -> {
            boolean ipaySuccess = results.getT1();
            boolean ibankingSuccess = results.getT2();

            // Liste des services disponibles et indisponibles
            List<ServiceStatus> serviceStatuses = List.of(
                    new ServiceStatus("i-pay", ipaySuccess, ipaySuccess ? "Service disponible" : "Service indisponible"),
                    new ServiceStatus("i-banking", ibankingSuccess, ibankingSuccess ? "Service disponible" : "Service indisponible")
            );

            if (ipaySuccess || ibankingSuccess) {
                String jwtToken = jwtUtil.generateToken(email);
                return new AuthResponse("success", "Authentification réussie", jwtToken, serviceStatuses);
            } else {
                return new AuthResponse("error", "Aucun service disponible", null, serviceStatuses);
            }
        });
    }
}



// package com.innov4africa.api_gateway.service;


// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import reactor.core.publisher.Mono;
// import com.innov4africa.api_gateway.model.AuthRequest;
// import com.innov4africa.api_gateway.model.AuthResponse;
// import com.innov4africa.api_gateway.model.ServiceStatus;

// import java.util.List;

// @Service
// public class AuthService {

//     @Autowired
//     private IPayService ipayService;

//     @Autowired
//     private IBankingService ibankingService;

//     @Autowired
//     private JwtUtil jwtUtil;

//     public Mono<AuthResponse> authenticate(AuthRequest request) {
//         String email = request.getEmail();
//         String password = request.getPassword();

//         // Authentification parallèle avec i-pay et i-banking
//         Mono<Boolean> ipayAuth = ipayService.authenticate(email, password);
//         Mono<Boolean> ibankingAuth = Mono.just(ibankingService.authenticate(email, password));

//         return Mono.zip(ipayAuth, ibankingAuth).map(results -> {
//             boolean ipaySuccess = results.getT1();
//             boolean ibankingSuccess = results.getT2();

//             // Liste des services disponibles et indisponibles
//             List<ServiceStatus> serviceStatuses = List.of(
//                     new ServiceStatus("i-pay", ipaySuccess, ipaySuccess ? "Service disponible" : "Service indisponible"),
//                     new ServiceStatus("i-banking", ibankingSuccess, ibankingSuccess ? "Service disponible" : "Service indisponible")
//             );

//             if (ipaySuccess || ibankingSuccess) {
//                 String jwtToken = jwtUtil.generateToken(email);
//                 return new AuthResponse("success", "Authentification réussie", jwtToken, serviceStatuses);
//             } else {
//                 return new AuthResponse("error", "Aucun service disponible", null, serviceStatuses);
//             }
//         });
//     }
// }


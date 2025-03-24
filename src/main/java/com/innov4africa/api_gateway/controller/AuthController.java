package com.innov4africa.api_gateway.controller;

import com.innov4africa.api_gateway.model.AuthRequest;
import com.innov4africa.api_gateway.model.AuthResponse;
import com.innov4africa.api_gateway.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
}


// package com.innov4africa.api_gateway.controller;

// import com.innov4africa.api_gateway.model.AuthRequest;
// import com.innov4africa.api_gateway.model.AuthResponse;
// import com.innov4africa.api_gateway.service.AuthService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import reactor.core.publisher.Mono;

// @RestController
// @RequestMapping("/auth")
// public class AuthController {

//     @Autowired
//     private AuthService authService;

//     @PostMapping("/login")
//     public Mono<AuthResponse> login(@RequestBody AuthRequest request) {
//         return authService.authenticate(request);
//     }
// }
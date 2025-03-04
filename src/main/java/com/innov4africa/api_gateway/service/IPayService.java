package com.innov4africa.api_gateway.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.util.Map;

@Service
public class IPayService {

    private final WebClient webClient;

    public IPayService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.ipay.com").build();
    }

    public Mono<Boolean> authenticate(String email, String password) {
        return webClient.post()
                .uri("/authenticate")
                .bodyValue(Map.of("email", email, "password", password))
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }
}
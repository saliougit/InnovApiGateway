package com.innov4africa.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

// @Configuration
// @EnableWebFluxSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//         return http
//                 .csrf().disable()
//                 .authorizeExchange(exchanges -> exchanges
//                         .pathMatchers("/auth/**").permitAll()
//                         .anyExchange().authenticated()
//                 )
//                 .build();
//     }
// }

// @Configuration
// @EnableWebFluxSecurity
// public class SecurityConfig {

//     @Bean
//     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//         return http
//                 .csrf().disable()
//                 .authorizeExchange(exchanges -> exchanges
//                         .pathMatchers("/auth/**").permitAll()
//                         .pathMatchers("/ipay/**").authenticated() // Autorise les endpoints /ipay avec authentification
//                         .anyExchange().authenticated()
//                 )
//                 .httpBasic().disable()
//                 .formLogin().disable()
//                 .build();
//     }
// }

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll() // Autorise tout sans authentification
                )
                .httpBasic().disable()
                .formLogin().disable()
                .build();
    }
}
package com.innov4africa.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// @Service
// public class JwtUtil {

//     @Value("${jwt.secret:myDefaultSecretKey12345678901234567890}")
//     private String secret;

//     @Value("${jwt.expiration:3600000}") // 1 heure par défaut
//     private long jwtExpiration;

//     private SecretKey getSigningKey() {
//         byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
//         return Keys.hmacShaKeyFor(keyBytes);
//     }

//     public String generateToken(String username) {
//         Map<String, Object> claims = new HashMap<>();
//         return createToken(claims, username);
//     }

//     private String createToken(Map<String, Object> claims, String subject) {
//         return Jwts.builder()
//                 .claims(claims)
//                 .subject(subject)
//                 .issuedAt(new Date(System.currentTimeMillis()))
//                 .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                 .signWith(getSigningKey())
//                 .compact();
//     }

//     public Boolean validateToken(String token) {
//         try {
//             Jwts.parser()
//                     .verifyWith(getSigningKey())
//                     .build()
//                     .parseSignedClaims(token);
//             return true;
//         } catch (Exception e) {
//             return false;
//         }
//     }

//     public String extractUsername(String token) {
//         return extractClaim(token, Claims::getSubject);
//     }

//     public Date extractExpiration(String token) {
//         return extractClaim(token, Claims::getExpiration);
//     }

//     public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//         final Claims claims = extractAllClaims(token);
//         return claimsResolver.apply(claims);
//     }

//     private Claims extractAllClaims(String token) {
//         return Jwts.parser()
//                 .verifyWith(getSigningKey())
//                 .build()
//                 .parseSignedClaims(token)
//                 .getPayload();
//     }

//     private Boolean isTokenExpired(String token) {
//         return extractExpiration(token).before(new Date());
//     }
// }


@Service
public class JwtUtil {

    @Value("${jwt.secret:myDefaultSecretKey12345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:3600000}") // 1 heure par défaut
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Génère un token avec des claims supplémentaires pour IPay
    public String generateIpayToken(String username, String ipayToken, String telephone, String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("ipayToken", ipayToken);
        claims.put("telephone", telephone);
        claims.put("userId", userId);
        return createToken(claims, username);
    }

    // Version basique (conservée pour compatibilité)
    public String generateToken(String username) {
        return createToken(new HashMap<>(), username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Méthodes d'extraction spécifiques à IPay
    public String extractIpayToken(String token) {
        return extractClaim(token, claims -> claims.get("ipayToken", String.class));
    }

    public String extractTelephone(String token) {
        return extractClaim(token, claims -> claims.get("telephone", String.class));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    // Méthodes existantes conservées
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
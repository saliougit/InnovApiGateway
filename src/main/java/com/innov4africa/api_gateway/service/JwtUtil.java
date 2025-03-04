package com.innov4africa.api_gateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Clé secrète pour signer le JWT (à remplacer par une clé sécurisée en production)
    private final String SECRET_KEY = "votre_clé_secrète_sécurisée";

    // Durée de validité du token (10 heures ici)
    private final long JWT_TOKEN_VALIDITY = 10 * 60 * 60 * 1000;

    /**
     * Génère un token JWT pour un utilisateur.
     *
     * @param email L'email de l'utilisateur.
     * @return Le token JWT généré.
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // Sujet du token (ici, l'email de l'utilisateur)
                .setIssuedAt(new Date()) // Date de création du token
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY)) // Date d'expiration
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Algorithme de signature
                .compact(); // Génère le token sous forme de chaîne
    }

    /**
     * Valide un token JWT.
     *
     * @param token Le token JWT à valider.
     * @param email L'email de l'utilisateur.
     * @return true si le token est valide, false sinon.
     */
    public Boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    /**
     * Extrait l'email du token JWT.
     *
     * @param token Le token JWT.
     * @return L'email extrait du token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token Le token JWT.
     * @return La date d'expiration.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait une information spécifique du token JWT.
     *
     * @param token          Le token JWT.
     * @param claimsResolver Fonction pour extraire l'information souhaitée.
     * @return L'information extraite.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait toutes les informations du token JWT.
     *
     * @param token Le token JWT.
     * @return Les informations du token.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Vérifie si le token JWT est expiré.
     *
     * @param token Le token JWT.
     * @return true si le token est expiré, false sinon.
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
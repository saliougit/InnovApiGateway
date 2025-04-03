package com.innov4africa.api_gateway.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository pour stocker les sessions utilisateur et leurs informations associées
 */
@Repository
public class UserSessionRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserSessionRepository.class);
    
    // Map pour stocker les informations de session utilisateur par token IPay
    private final Map<String, UserSessionInfo> userSessionMap = new ConcurrentHashMap<>();
    
    public void saveUserSession(String ipayToken, String userId, String telephone) {
        logger.info("Sauvegarde des informations de session pour le token: {}", ipayToken);
        userSessionMap.put(ipayToken, new UserSessionInfo(userId, telephone));
    }
    
    public UserSessionInfo getUserSessionInfo(String ipayToken) {
        return userSessionMap.get(ipayToken);
    }
    
    public boolean hasSessionInfo(String ipayToken) {
        return userSessionMap.containsKey(ipayToken);
    }
    
    /**
     * Classe interne pour stocker les informations d'une session utilisateur
     */
    public static class UserSessionInfo {
        private final String userId;
        private final String telephone;
        
        public UserSessionInfo(String userId, String telephone) {
            this.userId = userId;
            this.telephone = telephone;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getTelephone() {
            return telephone;
        }
    }
}
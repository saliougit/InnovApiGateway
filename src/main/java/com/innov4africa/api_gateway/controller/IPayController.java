
package com.innov4africa.api_gateway.controller;

import com.innov4africa.api_gateway.model.ServiceStatus;
import com.innov4africa.api_gateway.model.SoldeResponse;
import com.innov4africa.api_gateway.service.IPayService;
import com.innov4africa.api_gateway.service.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import reactor.core.publisher.Mono;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.List;

@RestController
@RequestMapping("/ipay")
public class IPayController {

    private static final Logger logger = LoggerFactory.getLogger(IPayController.class);
    
    @Autowired
    private IPayService ipayService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/solde")
    public Mono<ResponseEntity<SoldeResponse>> getSolde(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 1. Vérification de la présence du header Authorization
        if (authHeader == null || authHeader.isBlank()) {
            logger.warn("Tentative d'accès sans header Authorization");
            return buildUnauthorizedResponse("Token d'authentification manquant");
        }

        // 2. Vérification du format Bearer
        if (!authHeader.startsWith("Bearer ")) {
            logger.warn("Format de token invalide: {}", authHeader);
            return buildUnauthorizedResponse("Format de token invalide");
        }

        String jwt = authHeader.substring(7);
        
        // 3. Validation du token JWT
        if (!jwtUtil.validateToken(jwt)) {
            logger.warn("Token JWT invalide ou expiré");
            return buildUnauthorizedResponse("Token invalide ou expiré");
        }

        // 4. Extraction des claims
        String telephone = jwtUtil.extractTelephone(jwt);
        String ipayToken = jwtUtil.extractIpayToken(jwt);
        
        if (telephone == null || ipayToken == null) {
            logger.warn("Token ne contient pas les claims requis - telephone: {}, ipayToken: {}", telephone, ipayToken);
            return buildUnauthorizedResponse("Token incomplet");
        }

        logger.info("Demande de solde pour le téléphone: {}", telephone);
        
        // 5. Appel du service IPay
        return ipayService.getSolde(telephone, ipayToken)
            .flatMap(this::handleSoapResponse)
            .onErrorResume(this::handleError);
    }

    private Mono<ResponseEntity<SoldeResponse>> handleSoapResponse(String xmlResponse) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlResponse)));
            
            XPath xpath = XPathFactory.newInstance().newXPath();
            String error = xpath.evaluate("//return/error", doc);
            String message = xpath.evaluate("//return/message", doc);
            String montant = xpath.evaluate("//return/montant", doc);

            if ("0".equals(error)) {
                logger.info("Solde récupéré avec succès: {}", montant);
                return Mono.just(ResponseEntity.ok(
                    new SoldeResponse(
                        "success",
                        message,
                        montant,
                        List.of(new ServiceStatus("i-pay", true, "Solde récupéré"))
                )));
            } else {
                logger.warn("Erreur IPay: {}", message);
                return Mono.just(ResponseEntity.badRequest().body(
                    new SoldeResponse(
                        "error",
                        message,
                        "0.00",
                        List.of(new ServiceStatus("i-pay", false, message)))
                ));
            }
        } catch (Exception e) {
            logger.error("Erreur de traitement XML", e);
            return buildErrorResponse("Erreur de traitement de la réponse");
        }
    }

    private Mono<ResponseEntity<SoldeResponse>> handleError(Throwable e) {
        logger.error("Erreur lors de l'appel IPay", e);
        return buildErrorResponse("Erreur du service IPay");
    }

    private Mono<ResponseEntity<SoldeResponse>> buildUnauthorizedResponse(String message) {
        return Mono.just(ResponseEntity.status(401).body(
            new SoldeResponse(
                "error",
                message,
                "0.00",
                List.of(new ServiceStatus("i-pay", false, "Non autorisé"))
            )
        ));
    }

    private Mono<ResponseEntity<SoldeResponse>> buildErrorResponse(String message) {
        return Mono.just(ResponseEntity.internalServerError().body(
            new SoldeResponse(
                "error",
                message,
                "0.00",
                List.of(new ServiceStatus("i-pay", false, "Erreur technique"))
            )
        ));
    }
}

// package com.innov4africa.api_gateway.controller;

// import com.innov4africa.api_gateway.model.ServiceStatus;
// import com.innov4africa.api_gateway.model.SoldeResponse;
// import com.innov4africa.api_gateway.service.IPayService;
// import com.innov4africa.api_gateway.service.JwtUtil;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import org.w3c.dom.Document;
// import org.xml.sax.InputSource;
// import reactor.core.publisher.Mono;

// import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.xpath.XPath;
// import javax.xml.xpath.XPathFactory;
// import java.io.StringReader;
// import java.util.List;

// @RestController
// @RequestMapping("/ipay")
// public class IPayController {

//     @Autowired
//     private IPayService ipayService;
    
//     @Autowired
//     private JwtUtil jwtUtil;

//     @GetMapping("/solde")
//     public Mono<ResponseEntity<SoldeResponse>> getSolde(@RequestHeader("Authorization") String authHeader) {
//         String jwt = authHeader.substring(7); // Remove "Bearer "
//         String telephone = jwtUtil.extractTelephone(jwt);
//         String ipayToken = jwtUtil.extractIpayToken(jwt);
        
//         return ipayService.getSolde(telephone, ipayToken)
//             .flatMap(xmlResponse -> {
//                 try {
//                     // Parse XML response
//                     Document doc = DocumentBuilderFactory.newInstance()
//                             .newDocumentBuilder()
//                             .parse(new InputSource(new StringReader(xmlResponse)));
                    
//                     XPath xpath = XPathFactory.newInstance().newXPath();
//                     String error = xpath.evaluate("//return/error", doc);
//                     String message = xpath.evaluate("//return/message", doc);
//                     String montant = xpath.evaluate("//return/montant", doc);
                    
//                     if ("0".equals(error)) {
//                         SoldeResponse response = new SoldeResponse(
//                             "success",
//                             message,
//                             montant,
//                             List.of(new ServiceStatus("i-pay", true, "Solde récupéré avec succès"))
//                         );
//                         return Mono.just(ResponseEntity.ok(response));
//                     } else {
//                         SoldeResponse response = new SoldeResponse(
//                             "error",
//                             message,
//                             "0",
//                             List.of(new ServiceStatus("i-pay", false, message))
//                         );
//                         return Mono.just(ResponseEntity.status(400).body(response));
//                     }
//                 } catch (Exception e) {
//                     SoldeResponse errorResponse = new SoldeResponse(
//                         "error",
//                         "Erreur technique: " + e.getMessage(),
//                         "0",
//                         List.of(new ServiceStatus("i-pay", false, "Erreur de traitement"))
//                     );
//                     return Mono.just(ResponseEntity.status(500).body(errorResponse));
//                 }
//             });
//     }
// }

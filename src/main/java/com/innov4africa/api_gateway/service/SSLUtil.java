// package com.innov4africa.api_gateway.service;

// import javax.net.ssl.*;
// import java.security.SecureRandom;

// public class SSLUtil {
//     public static void disableSslVerification() {
//         try {
//             TrustManager[] trustAllCerts = new TrustManager[]{
//                 new X509TrustManager() {
//                     public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                         return null;
//                     }
//                     public void checkClientTrusted(
//                         java.security.cert.X509Certificate[] certs, String authType) {
//                     }
//                     public void checkServerTrusted(
//                         java.security.cert.X509Certificate[] certs, String authType) {
//                     }
//                 }
//             };
//             SSLContext sc = SSLContext.getInstance("SSL");
//             sc.init(null, trustAllCerts, new SecureRandom());
//             HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//     }
// }


package com.innov4africa.api_gateway.service;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SSLUtil {
    public static void disableSslVerification() {
        try {
            // Créer un trust manager qui ne valide pas les chaînes de certificats
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            // Installer le trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Créer un hostname verifier qui accepte tous les noms d'hôte
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to disable SSL verification", e);
        }
    }
}
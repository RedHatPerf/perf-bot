package io.perf.tools.bot.service.datastore.horreum;

import io.hyperfoil.tools.HorreumClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;

@Startup
public class Horreum {

    static TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }
    };

    @ConfigProperty(name = "proxy.datastore.horreum.url")
    String horreumUrl;

    @ConfigProperty(name = "proxy.datastore.horreum.truststore.trust-all")
    Boolean useTrustAllCerts;

    @ConfigProperty(name = "proxy.datastore.horreum.truststore.file", defaultValue = "")
    Optional<String> trustStoreFile;

    @ConfigProperty(name = "proxy.datastore.horreum.truststore.pwd")
    Optional<String> trustStorePwd;

    public SSLContext horreumSslContext() {
        SSLContext sc = null;
        try {
            if (useTrustAllCerts) {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());

                SSLContext.setDefault(sc);
            } else if (trustStoreFile.isPresent() && !trustStoreFile.get().isBlank()) {
                KeyStore trustStore = KeyStore.getInstance("JKS");
                trustStore.load(new FileInputStream(trustStoreFile.get()), trustStorePwd.orElse("").toCharArray());

                // Create SSLContext from truststore
                sc = SSLContextBuilder.create()
                        .loadTrustMaterial(trustStore, null)
                        .build();
            }
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | IOException | CertificateException e) {
            Log.error("Error initializing SSL context", e);
            throw new RuntimeException("Error initializing SSL context");
        }
        return sc;
    }

    @Produces
    @Default
    public HorreumClient.Builder horreumClientBuilder() {
        return new HorreumClient.Builder().horreumUrl(horreumUrl).sslContext(horreumSslContext());
    }
}

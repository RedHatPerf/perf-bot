package io.perf.tools.bot.service.job.jenkins;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Produces;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;

@Startup
public class Jenkins {

    @ConfigProperty(name = "proxy.job.runner.jenkins.truststore.file")
    Optional<String> trustStoreFile;

    @ConfigProperty(name = "proxy.job.runner.jenkins.truststore.pwd")
    Optional<String> trustStorePwd;

    @Produces
    public HttpClientBuilder jenkinsHttpClientBuilder()
            throws KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = SSLContextBuilder.create();

        if (trustStoreFile.isPresent() && !trustStoreFile.get().isBlank()) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(trustStoreFile.get()), trustStorePwd.orElse("").toCharArray());
            sslContextBuilder.loadTrustMaterial(trustStore, null);
        }

        SSLContext sslContext = sslContextBuilder.build();

        // Create a custom HttpClient builder with the SSL context
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        return HttpClientBuilder.create().setSSLSocketFactory(sslSocketFactory);
    }
}

package io.lampajr.proxy;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.inject.Produces;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Startup
public class Jenkins {

    @ConfigProperty(name = "proxy.jenkins.url")
    String url;

    @ConfigProperty(name = "proxy.jenkins.user")
    String user;

    @ConfigProperty(name = "proxy.jenkins.apiKey")
    String apiKey;

    @ConfigProperty(name = "proxy.jenkins.truststore.file")
    String truststore;

    @ConfigProperty(name = "proxy.jenkins.truststore.pwd")
    String truststorePwd;

    @Produces
    public JenkinsServer jenkinsServer()
            throws URISyntaxException, KeyStoreException, NoSuchAlgorithmException, IOException, CertificateException,
            KeyManagementException {
        if (url == null || user == null || apiKey == null) {
            Log.error("Incorrect configuration for Jenkins instance");
            return null;
        }
        URI uri = new URI(url);

        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(truststore), truststorePwd.toCharArray());

        // Create SSLContext from truststore
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStore, null)
                .build();

        // Create a custom HttpClient builder with the SSL context
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().setSSLSocketFactory(sslSocketFactory);

        // Create Jenkins client with the custom HttpClient
        JenkinsHttpClient client = new JenkinsHttpClient(uri, httpClientBuilder, user, apiKey);

        return new JenkinsServer(client);
    }
}

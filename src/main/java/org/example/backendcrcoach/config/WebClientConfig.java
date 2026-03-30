package org.example.backendcrcoach.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ...existing code... (removed unused TimeUnit import)
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // connect=10s, response=30min, read=10min por defecto
        return webClientBuilder(10000, 1800, 600);
    }


    // Método con parámetros para facilitar tests y configuración externa
    public WebClient.Builder webClientBuilder(
            @Value("${http.client.connect-timeout-ms:10000}") Integer connectTimeoutMillis,
            @Value("${http.client.response-timeout-seconds:1800}") Integer responseTimeoutSeconds,
            @Value("${http.client.read-timeout-seconds:10000}") Integer readTimeoutSeconds) {

        Logger log = LoggerFactory.getLogger(WebClientConfig.class);
        log.info("Configuring WebClient HttpClient timeouts: connect={}ms response={}s read={}s",
                connectTimeoutMillis, responseTimeoutSeconds, readTimeoutSeconds);

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis);

        httpClient = httpClient.responseTimeout(Duration.ofSeconds(responseTimeoutSeconds));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
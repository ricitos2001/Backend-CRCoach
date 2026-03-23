package org.example.backendcrcoach.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
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

        HttpClient httpClient = HttpClient.create()
                // Timeout de conexión (10s)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)

                // Timeout de respuesta total (5 minutos)
                .responseTimeout(Duration.ofMinutes(10))

                // Timeout de lectura (5 minutos sin recibir datos)
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(300))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
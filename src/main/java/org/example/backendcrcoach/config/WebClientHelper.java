package org.example.backendcrcoach.config;

import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class WebClientHelper {

    private static final Logger log = LoggerFactory.getLogger(WebClientHelper.class);

    private final int maxRetries;
    private final Duration blockTimeout;

    public WebClientHelper(@Value("${webclient.max-retries:3}") int maxRetries,
                           @Value("${webclient.block-timeout-seconds:30}") int blockTimeoutSeconds) {
        this.maxRetries = maxRetries;
        this.blockTimeout = Duration.ofSeconds(blockTimeoutSeconds);
    }

    public String fetchGetWithRetries(WebClient client, String uri, Object... uriVars) {
        try {
            return client.get()
                    .uri(uri, uriVars)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1)).filter(this::isRetryable))
                    .block(blockTimeout);
        } catch (RuntimeException e) {
            if (isReadTimeout(e)) {
                log.warn("Read timeout contacting {} ({}): {}", uri, java.util.Arrays.toString(uriVars), e.getMessage());
                throw new IllegalStateException("Timeout contacting remote API (read timeout)", e);
            }
            throw e;
        }
    }

    private boolean isRetryable(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            if (curr instanceof ReadTimeoutException) return true;
            String cn = curr.getClass().getName();
            if (cn.contains("ReadTimeoutException") || cn.contains("TimeoutException") || cn.contains("ConnectTimeoutException")) return true;
            curr = curr.getCause();
        }
        return false;
    }

    private boolean isReadTimeout(Throwable t) {
        Throwable curr = t;
        while (curr != null) {
            if (curr instanceof ReadTimeoutException) return true;
            String cn = curr.getClass().getName();
            if (cn.contains("ReadTimeoutException") || cn.contains("TimeoutException")) return true;
            curr = curr.getCause();
        }
        return false;
    }
}


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

    public WebClientHelper(@Value("${webclient.max-retries:10}") int maxRetries,
                           @Value("${webclient.block-timeout-seconds:10000}") int blockTimeoutSeconds) {
        this.maxRetries = maxRetries;
        this.blockTimeout = Duration.ofSeconds(blockTimeoutSeconds);
    }

    public String fetchGetWithRetries(WebClient client, String uri, Object... uriVars) {
        try {
            return client.get()
                    .uri(uri, uriVars)
                    .retrieve()
                    .bodyToMono(String.class)
                    // Apply Reactor-level timeout per attempt to avoid Netty-level ReadTimeoutHandler
                    .timeout(blockTimeout)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .jitter(0.5)
                            .filter(this::isRetryable))
                    // Map read-timeout related errors to a clean IllegalStateException without keeping
                    // the original Netty exception as cause (avoids io.netty stacktrace in logs).
                    .onErrorMap(throwable -> {
                        if (isReadTimeout(throwable)) {
                            return new IllegalStateException("Timeout contacting remote API (read timeout)");
                        }
                        // preserve other exceptions
                        return throwable instanceof RuntimeException ? (RuntimeException) throwable : new RuntimeException(throwable);
                    })
                    .block();
        } catch (Throwable t) {
            // Log the full cause chain for easier debugging
            StringBuilder causes = new StringBuilder();
            Throwable curr = t;
            while (curr != null) {
                causes.append(curr.getClass().getName()).append(": ").append(curr.getMessage()).append(" <- ");
                curr = curr.getCause();
            }
            log.warn("Error contacting {} ({}). Cause chain: {}", uri, java.util.Arrays.toString(uriVars), causes.toString());

            if (isReadTimeout(t)) {
                // Throw a clean exception (without including Netty internals as cause)
                throw new IllegalStateException("Timeout contacting remote API (read timeout)");
            }

            if (t instanceof RuntimeException) throw (RuntimeException) t;
            throw new RuntimeException(t);
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


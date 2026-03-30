package org.example.backendcrcoach.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SchedulingTestComponent {

    private static final Logger log = LoggerFactory.getLogger(SchedulingTestComponent.class);

    // Ejecuta de forma periódica según la propiedad test.scheduling.delay-ms
    @Scheduled(fixedDelayString = "${test.scheduling.delay-ms:5000}")
    public void heartbeat() {
        log.info("[scheduling-test] heartbeat at {}", Instant.now());
    }
}


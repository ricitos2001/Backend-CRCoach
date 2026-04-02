package org.example.backendcrcoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class BackendCrCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendCrCoachApplication.class, args);
    }

}

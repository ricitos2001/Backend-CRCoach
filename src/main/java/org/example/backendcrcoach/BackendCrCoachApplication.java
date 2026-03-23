package org.example.backendcrcoach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendCrCoachApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendCrCoachApplication.class, args);
    }

}

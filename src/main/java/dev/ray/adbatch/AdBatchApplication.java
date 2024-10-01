package dev.ray.adbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class AdBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdBatchApplication.class, args);
    }
}

package com.zoplanner.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {

    @Bean
    public SesClient sesClient(@Value("${aws.region:eu-north-1}") String region) {
        return SesClient.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();
    }
}

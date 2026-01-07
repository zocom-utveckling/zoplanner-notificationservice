package com.zoplanner.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesConfig {

    /**
     * Region for SES. We give it a sensible default so tests don't fail
     * when aws.region isn't provided in application-test properties.
     */
    @Value("${aws.region:eu-north-1}")

    private String region;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(region))
                .build();
    }
}

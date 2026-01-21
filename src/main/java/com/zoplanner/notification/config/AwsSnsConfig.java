package com.zoplanner.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsSnsConfig {

    @Bean
    public SnsClient snsClient(){
        return SnsClient.builder()
                .region(Region.EU_NORTH_1)
                .build();
    }

}

package com.memevote.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * AWS Configuration for production and staging environments
 * Configures AWS services including Secrets Manager for database credentials
 */
@Configuration
@ConditionalOnProperty(name = "aws.region")
public class AwsConfig {

    @Value("${aws.region:ca-central-1}")
    private String awsRegion;

    /**
     * Configure AWS Secrets Manager client
     * Uses default credentials provider chain (instance profile when running on ECS)
     */
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

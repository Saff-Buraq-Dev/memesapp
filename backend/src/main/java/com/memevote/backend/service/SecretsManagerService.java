package com.memevote.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import javax.annotation.PostConstruct;

/**
 * Service to retrieve database credentials from AWS Secrets Manager
 */
@Service
@ConditionalOnProperty(name = "db.secret.name")
public class SecretsManagerService {

    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerService.class);

    @Autowired
    private SecretsManagerClient secretsManagerClient;

    @Value("${db.secret.name:}")
    private String secretName;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String username;
    private String password;

    @PostConstruct
    public void loadCredentials() {
        if (secretName == null || secretName.trim().isEmpty()) {
            logger.warn("DB_SECRET_NAME not provided, skipping Secrets Manager initialization");
            return;
        }

        try {
            logger.info("Loading database credentials from Secrets Manager: {}", secretName);

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
            String secretString = response.secretString();

            // Parse the JSON secret
            JsonNode secretJson = objectMapper.readTree(secretString);
            this.username = secretJson.get("username").asText();
            this.password = secretJson.get("password").asText();

            logger.info("Successfully loaded database credentials for user: {}", username);

        } catch (SecretsManagerException e) {
            logger.error("Failed to retrieve secret from Secrets Manager: {}", e.getMessage());
            throw new RuntimeException("Failed to load database credentials", e);
        } catch (Exception e) {
            logger.error("Error parsing secret from Secrets Manager: {}", e.getMessage());
            throw new RuntimeException("Failed to parse database credentials", e);
        }
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isInitialized() {
        return username != null && password != null;
    }
}

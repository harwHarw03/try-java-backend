package com.airscope.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * DynamoDbConfig - sets up the DynamoDB connection.
 *
 * We support two modes:
 *   1. Local mode (aws.dynamodb.local=true)  → connects to DynamoDB Local on your machine
 *   2. AWS mode  (aws.dynamodb.local=false) → connects to real AWS DynamoDB
 *
 * For development, use DynamoDB Local (it's free and runs offline).
 * For production, use real AWS credentials.
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey}")
    private String secretAccessKey;

    @Value("${aws.dynamodb.local}")
    private boolean isLocal;

    @Value("${aws.dynamodb.local.endpoint}")
    private String localEndpoint;

    /**
     * Creates the low-level DynamoDB client.
     * The Enhanced Client (below) wraps this to provide object mapping.
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (isLocal) {
            builder.endpointOverride(URI.create(localEndpoint));
        }

        return builder.build();
    }

    /**
     * Creates the Enhanced DynamoDB client.
     * This is what we use in SensorDataRepository — it maps Java objects to DynamoDB items.
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}

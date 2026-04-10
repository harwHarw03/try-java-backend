package com.airscope.config;

import com.airscope.dynamodb.SensorData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbConfig.class);

    @Value("${aws.region}")
    private String region;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretAccessKey:}")
    private String secretAccessKey;

    @Value("${aws.dynamodb.local:true}")
    private boolean isLocal;

    @Value("${aws.dynamodb.local.endpoint:http://localhost:8000}")
    private String localEndpoint;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region));

        if (isLocal) {
            builder.endpointOverride(URI.create(localEndpoint))
                   .credentialsProvider(StaticCredentialsProvider.create(
                           AwsBasicCredentials.create("dummy", "dummy")));
        } else if (accessKeyId != null && !accessKeyId.isBlank()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)));
        }

        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbInitializer dynamoDbInitializer(DynamoDbClient dynamoDbClient) {
        return new DynamoDbInitializer(dynamoDbClient, isLocal);
    }

    public static class DynamoDbInitializer {

        private static final Logger log = LoggerFactory.getLogger(DynamoDbInitializer.class);

        private final DynamoDbClient dynamoDbClient;
        private final boolean isLocal;

        public DynamoDbInitializer(DynamoDbClient dynamoDbClient, boolean isLocal) {
            this.dynamoDbClient = dynamoDbClient;
            this.isLocal = isLocal;
        }

        @PostConstruct
        public void createTableIfNotExists() {
            if (isLocal) {
                try {
                    dynamoDbClient.describeTable(DescribeTableRequest.builder()
                            .tableName(SensorData.TABLE_NAME)
                            .build());
                    log.info("DynamoDB table '{}' already exists", SensorData.TABLE_NAME);
                } catch (ResourceNotFoundException e) {
                    log.info("Creating DynamoDB table '{}'...", SensorData.TABLE_NAME);
                    createSensorDataTable();
                } catch (Exception e) {
                    log.warn("Could not verify/create DynamoDB table '{}': {}", SensorData.TABLE_NAME, e.getMessage());
                }
            }
        }

        private void createSensorDataTable() {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(SensorData.TABLE_NAME)
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("deviceId")
                                    .keyType(KeyType.HASH)
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("timestamp")
                                    .keyType(KeyType.RANGE)
                                    .build()
                    )
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("deviceId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("timestamp")
                                    .attributeType(ScalarAttributeType.S)
                                    .build()
                    )
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(request);
            log.info("DynamoDB table '{}' created successfully", SensorData.TABLE_NAME);
        }
    }
}

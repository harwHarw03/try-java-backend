package com.airscope.dynamodb;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DynamoDB repository for SensorData.
 *
 * This is NOT a JPA repository — DynamoDB uses a different SDK.
 * We use the "Enhanced Client" which maps Java objects to DynamoDB items automatically.
 */
@Repository
@RequiredArgsConstructor // Lombok: generates constructor for final fields (used for injection)
public class SensorDataRepository {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;

    // Table name must match what's in your AWS account
    private static final String TABLE_NAME = "sensor_data";

    /**
     * Gets the DynamoDB table object.
     * TableSchema.fromBean() uses the @DynamoDbBean annotations to understand the structure.
     */
    private DynamoDbTable<SensorData> getTable() {
        return dynamoDbEnhancedClient.table(TABLE_NAME, TableSchema.fromBean(SensorData.class));
    }

    /**
     * Save a single sensor reading to DynamoDB.
     */
    public void save(SensorData sensorData) {
        getTable().putItem(sensorData);
    }

    /**
     * Get all readings for a specific device.
     * Results are sorted by timestamp (newest first won't work with DynamoDB default,
     * so we reverse after fetching).
     *
     * @param deviceId the device to fetch readings for
     * @param limit    max number of readings to return
     */
    public List<SensorData> findByDeviceId(String deviceId, int limit) {
        // QueryConditional.keyEqualTo means "where deviceId = X"
        QueryConditional queryCondition = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(deviceId).build());

        // Build the query with a limit
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryCondition)
                .limit(limit)
                .scanIndexForward(false) // false = descending order (newest first)
                .build();

        // Execute query and collect results into a list
        return getTable().query(request)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}

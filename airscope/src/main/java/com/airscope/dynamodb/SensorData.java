package com.airscope.dynamodb;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * SensorData - stored in DynamoDB (NoSQL).
 *
 * DynamoDB is great for high-frequency time-series data because:
 * - It scales automatically
 * - Reads/writes are very fast
 * - No schema constraints (flexible attributes)
 *
 * Design:
 *   Partition Key = deviceId  → all readings from same device are grouped together
 *   Sort Key      = timestamp → within a device, data is sorted by time
 *
 * This lets us efficiently query: "get last 100 readings from device X"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean // tells DynamoDB Enhanced Client this is a table-mapped class
public class SensorData {

    private String deviceId;   // Partition Key - which device sent this
    private String timestamp;  // Sort Key - when it was sent (ISO 8601 format)

    private Double temperature; // °C
    private Double humidity;    // % relative humidity
    private Double co2;         // ppm (parts per million)
    private Double pm25;        // µg/m³ (micrograms per cubic meter)

    @DynamoDbPartitionKey
    public String getDeviceId() {
        return deviceId;
    }

    @DynamoDbSortKey
    public String getTimestamp() {
        return timestamp;
    }
}

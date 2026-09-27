package com.practice.inventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import com.practice.inventoryservice.domain.InventoryItem;

import java.net.URI;

@Configuration
public class DynamoDbConfig {

    @Value("${dynamodb.endpoint}")
    private String endpoint;

    @Value("${dynamodb.region}")
    private String region;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("fakeMyKeyId", "fakeSecretAccessKey")))
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient client) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();
    }

    @Bean
    public DynamoDbTable<InventoryItem> inventoryTable(DynamoDbEnhancedClient enhancedClient, DynamoDbClient client) {
        DynamoDbTable<InventoryItem> table = enhancedClient.table("InventoryItems", TableSchema.fromBean(InventoryItem.class));
        ensureTableExists(client, "InventoryItems", table);
        return table;
    }

    private void ensureTableExists(DynamoDbClient client, String tableName, DynamoDbTable<InventoryItem> table) {
        try {
            client.describeTable(DescribeTableRequest.builder().tableName(tableName).build());
        } catch (ResourceNotFoundException e) {
            table.createTable(builder -> builder.provisionedThroughput(
                    ProvisionedThroughput.builder().readCapacityUnits(5L).writeCapacityUnits(5L).build()));
        }
    }
}

package com.practice.inventoryservice.domain;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InventoryRepository {

    private final DynamoDbTable<InventoryItem> table;

    public InventoryRepository(DynamoDbTable<InventoryItem> table) {
        this.table = table;
    }

    public Optional<InventoryItem> findBySku(String sku) {
        return Optional.ofNullable(table.getItem(r -> r.key(k -> k.partitionValue(sku))));
    }

    public void save(InventoryItem item) {
        table.putItem(item);
    }
}

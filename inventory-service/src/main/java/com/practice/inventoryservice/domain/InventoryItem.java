package com.practice.inventoryservice.domain;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class InventoryItem {

    private String sku;
    private int quantityAvailable;

    public InventoryItem() {}

    public InventoryItem(String sku, int quantityAvailable) {
        this.sku = sku;
        this.quantityAvailable = quantityAvailable;
    }

    @DynamoDbPartitionKey
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(int quantityAvailable) { this.quantityAvailable = quantityAvailable; }
}

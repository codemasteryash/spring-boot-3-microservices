package com.techie.microservices.order.service;

import com.techie.microservices.order.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryClient inventoryClient;

    @CircuitBreaker(name = "inventory", fallbackMethod = "fallback")
    @Retry(name = "inventory")
    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryClient.isInStock(skuCode, quantity);
    }

    public boolean fallback(String skuCode,
                            Integer quantity,
                            Throwable throwable) {
        log.error("Inventory service failed for skuCode={}, reason={}",
                skuCode, throwable.getMessage());
        return false;
    }
}

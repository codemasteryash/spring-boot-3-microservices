package com.techie.microservices.order.service;

import com.techie.microservices.order.dto.OrderRequest;
import com.techie.microservices.order.event.OrderPlacedEvent;
import com.techie.microservices.order.model.Order;
import com.techie.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService; // ✅ USE SERVICE, NOT CLIENT
    private final KafkaTemplate<String,OrderPlacedEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest) {

        boolean isProductInStock =
                inventoryService.isInStock(
                        orderRequest.skuCode(),
                        orderRequest.quantity()
                );

        if (isProductInStock) {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);

//            send message to kafka topic
            OrderPlacedEvent orderPlacedEvent= new OrderPlacedEvent(order.getOrderNumber()
                    ,orderRequest.userDetails().email(),
                    orderRequest.userDetails().firstName(),
                    orderRequest.userDetails().lastName());
            log.info("Start - Sending OrderPlacedEvent {} to kafka topic order-placed",orderPlacedEvent);
            kafkaTemplate.send("order-placed",orderPlacedEvent);
            log.info("End - Sending OrderPlacedEvent {} to kafka topic order-placed",orderPlacedEvent);

        } else {
            throw new RuntimeException(
                    "Product with SKU Code " + orderRequest.skuCode() + " is not in stock"
            );
        }
    }
}


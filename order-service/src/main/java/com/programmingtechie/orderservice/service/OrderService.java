package com.programmingtechie.orderservice.service;

import com.programmingtechie.orderservice.dto.InventoryResponse;
import com.programmingtechie.orderservice.dto.OrderLineItemsDto;
import com.programmingtechie.orderservice.dto.OrderRequest;
import com.programmingtechie.orderservice.model.Order;
import com.programmingtechie.orderservice.model.OrderLineItems;
import com.programmingtechie.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient.Builder webClientBuilder;

    private final Tracer tracer;

    public String placeOrder(OrderRequest orderRequest) {

        Order order = Order
                .builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderLineItemsList(
                        orderRequest.
                                getOrderLineItemsDtoList()
                                .stream()
                                .map(this::mapToEntity)
                                .toList()
                )
                .build();

        List<String> skyCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).toList();

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(inventoryServiceLookup.start())) {
            // Call inventory service, and place order if product is in stock
            InventoryResponse[] inventoryResponseArray = webClientBuilder.build()
                    .get()
                    .uri("http://inventory-service/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCode", skyCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class) // the returning class
                    .block();// to make it syncronous request

            boolean allProductsInStrock = Arrays
                    .stream(inventoryResponseArray)
                    .allMatch(InventoryResponse::isInStock);

            if (!allProductsInStrock)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is not in stock, please try again later");

            orderRepository.save(order);
            return "Order Placed Successfully";
        } finally {
            inventoryServiceLookup.end();
        }

    }

    private OrderLineItems mapToEntity(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems
                .builder()
                .price(orderLineItemsDto.getPrice())
                .quantity(orderLineItemsDto.getQuantity())
                .skuCode(orderLineItemsDto.getSkuCode())
                .build();
    }
}

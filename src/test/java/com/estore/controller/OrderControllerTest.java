package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.OrderItemWithProductResponseDto;
import com.estore.dto.response.OrderWithProductsResponseDto;
import com.estore.model.Product;
import com.estore.repository.ProductRepository;
import com.estore.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class {@link OrderControllerTest} provides integration tests for the {@link OrderController} class,
 * testing its API endpoints.
 * <p>The tests are performed using a test container with a PostgreSQL database.</p>
 * <p>{@link TestContainerConfig} is the class for test container configuration.</p>
 *
 * @author Dmytro Trotsenko on 4/21/23
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainerConfig.class)
public class OrderControllerTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int randomServerPort;

    private static final String URI = "/orders";

    private final Long NOT_EXISTED_ID = 100L;

    private final List<ProductRequestDto> products = List.of(
            new ProductRequestDto("laptop", "Lenovo", BigDecimal.valueOf(3550.95)),
            new ProductRequestDto("phone", "Xiaomi", BigDecimal.valueOf(6700.55)),
            new ProductRequestDto("smartTV", "Samsung", BigDecimal.valueOf(9670.19))
    );

    @BeforeEach
    public void setup() {
        String localHost = "http://localhost:";
        webTestClient = WebTestClient.bindToServer()
                .baseUrl(localHost + randomServerPort)
                .build();

        saveProductsIfNotExist(products);
    }

    @AfterEach
    public void cleanup() {
        orderService.deleteAll().subscribe();
    }

    //-----------------------------------
    //               GET
    //-----------------------------------

    @Test
    void shouldReturnEmptyListOfAllOrders() {

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderWithProductsResponseDto.class)
                .value(orderList -> assertTrue(orderList.isEmpty()));
    }

    @Test
    void shouldReturnAllOrdersWithProducts() {

        int ordersNum = 3;
        var savedOrders = createOrders(ordersNum);

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderWithProductsResponseDto.class)
                .value(orderList -> {
                    assertEquals(ordersNum, orderList.size());
                    assertOrderListEquals(savedOrders, orderList);
                });
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    private void saveProductsIfNotExist(List<ProductRequestDto> productList) {
        Flux.fromIterable(productList)
                .map(p -> new Product(null, p.getName(), p.getDescription(), p.getPrice()))
                .flatMap(p -> productRepository.findByName(p.getName())
                        .switchIfEmpty(Mono.defer(() -> productRepository.save(p))))
                .subscribe();
    }

    private List<OrderWithProductsResponseDto> createOrders(int num) {
        List<OrderWithProductsResponseDto> orders = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            orders.add(orderService.create().block());
        }
        return orders;
    }

    private void assertOrderListEquals(List<OrderWithProductsResponseDto> expectedOrders, List<OrderWithProductsResponseDto> actualOrders){
        IntStream.range(0, actualOrders.size())
                .forEach(i -> assertOrderEquals(expectedOrders.get(i), actualOrders.get(i)));
    }

    private void assertOrderEquals(OrderWithProductsResponseDto expectedOrder, OrderWithProductsResponseDto actualOrder) {
        assertEquals(expectedOrder.getDate(), actualOrder.getDate());
        assertOrderItemListEquals(expectedOrder.getOrderItems(), actualOrder.getOrderItems());
    }

    private void assertOrderItemListEquals(List<OrderItemWithProductResponseDto> expectedOrderItems, List<OrderItemWithProductResponseDto> actualOrderItems) {
        IntStream.range(0, actualOrderItems.size())
                .forEach(i -> assertOrderItemEquals(expectedOrderItems.get(i), actualOrderItems.get(i)));
    }

    private void assertOrderItemEquals(OrderItemWithProductResponseDto expectedOrderItem, OrderItemWithProductResponseDto actualOrderItem) {
        assertEquals(expectedOrderItem.getProduct(), actualOrderItem.getProduct());
        assertEquals(expectedOrderItem.getQuantity(), actualOrderItem.getQuantity());
    }

}

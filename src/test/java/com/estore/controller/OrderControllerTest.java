package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderItemWithProductResponseDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.model.Product;
import com.estore.model.User;
import com.estore.repository.ProductRepository;
import com.estore.repository.UserRepository;
import com.estore.service.OrderService;
import org.jetbrains.annotations.NotNull;
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
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderService orderService;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int randomServerPort;

    private static final String URI = "/orders";

    private final Long USER_ID = null;
    private final Long NOT_EXISTED_ORDER_ID = 100L;
    private final Long NOT_EXISTED_PRODUCT_ID = 100L;

    private final List<Product> products = List.of(
            new Product(1L, "laptop", "Lenovo", BigDecimal.valueOf(3550.95)),
            new Product(2L, "phone", "Xiaomi", BigDecimal.valueOf(6700.55)),
            new Product(3L, "smartTV", "Samsung", BigDecimal.valueOf(9670.19))
    );

    private final List<OrderItemRequestDto> orderItems = List.of(
            new OrderItemRequestDto(products.get(0).getId(), 1),
            new OrderItemRequestDto(products.get(1).getId(), 2),
            new OrderItemRequestDto(products.get(2).getId(), 3)
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
                .expectBodyList(OrderResponseDto.class)
                .value(orderList -> assertTrue(orderList.isEmpty()));
    }

    @Test
    void shouldReturnAllOrdersWithProducts() {

        int ordersNum = 3;
        var emptyOrders = createOrdersWithProducts(ordersNum);

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseDto.class)
                .value(orderList -> {
                    assertEquals(ordersNum, orderList.size());
                    assertOrderListEquals(emptyOrders, orderList);
                });
    }

    @Test
    void shouldReturnOrderById() {

        var savedOrderWithProducts = createOrdersWithProducts(3).get(0);
        Long id = savedOrderWithProducts.getId();

        webTestClient.get().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDto.class)
                .value(order -> assertOrderEquals(savedOrderWithProducts, order));
    }

    @Test
    void shouldThrowExceptionIfOrderIdDoesNotExist() {

        webTestClient.get().uri(URI.concat("/{id}"), NOT_EXISTED_ORDER_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               POST
    //-----------------------------------

    @Test
    void shouldCreatedNewOrder() {
        var user = new User(null, "User", "user@gmail.com", "+380999999999", "password");
        User newUser = userRepository.save(user)
                .block();

        assert newUser != null;
        Long userId = newUser.getId();

        var expectedOrder = new OrderResponseDto(null, LocalDate.now(), null);

        webTestClient.post().uri(URI.concat("/{userId}"), userId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDto.class)
                .value(order -> assertOrderEquals(expectedOrder, order));

        orderService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldAddedProductToOrderById() {

        var savedOrder = orderService.create(USER_ID).block();
        assertNotNull(savedOrder);
        Long id = savedOrder.getId();

        List<OrderItemWithProductResponseDto> addedOrderItemList = List.of(
                new OrderItemWithProductResponseDto(null, products.get(0), 1));
        var addedOrderWithProducts = new OrderResponseDto(null, LocalDate.now(), addedOrderItemList);

        webTestClient.post().uri(URI.concat("/add/{id}"), id)
                .bodyValue(orderItems.get(0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDto.class)
                .value(order -> assertOrderEquals(addedOrderWithProducts, order));
    }

    @Test
    void shouldSummarizingQuantityWhenAddedProductToOrderById() {

        OrderResponseDto orderWithProducts = orderService.create(USER_ID)
                .flatMap(o -> orderService.addProductByOrderId(o.getId(), new OrderItemRequestDto(1L, 1)))
                .block();

        assert orderWithProducts != null;
        Long id = orderWithProducts.getId();

        List<OrderItemWithProductResponseDto> addedOrderItemList = List.of(
                new OrderItemWithProductResponseDto(null, products.get(0), 2));

        var addedOrderWithProducts = new OrderResponseDto(null, LocalDate.now(), addedOrderItemList);

        webTestClient.post().uri(URI.concat("/add/{id}"), id)
                .bodyValue(orderItems.get(0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseDto.class)
                .value(order -> assertOrderEquals(addedOrderWithProducts, order));
    }

    @Test
    void shouldThrowExceptionIfAddedProductIdDoesNotExist() {

        var savedOrder = orderService.create(USER_ID).block();
        assertNotNull(savedOrder);
        Long id = savedOrder.getId();

        var orderItem = new OrderItemRequestDto(NOT_EXISTED_PRODUCT_ID, 100);

        webTestClient.post().uri(URI.concat("/add/{id}"), id)
                .bodyValue(orderItem)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldThrowExceptionAddedProductIfOrderIdDoesNotExist() {

        orderService.create(USER_ID).block();

        webTestClient.post().uri(URI.concat("/add/{id}"), NOT_EXISTED_ORDER_ID)
                .bodyValue(orderItems.get(1))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               PUT
    //-----------------------------------

    @Test
    void shouldUpdatedExistingOrder() {

        var savedOrderWithProducts = createOrdersWithProducts(3).get(0);
        Long id = savedOrderWithProducts.getId();
        LocalDate updatedDate = LocalDate.now().minusDays(3);
        var orderForUpdate = new OrderRequestDto(updatedDate, orderItems.subList(0, 1));


        List<OrderItemWithProductResponseDto> updatedOrderItemList = List.of(
                new OrderItemWithProductResponseDto(null, products.get(0), 1));
        var updatedOrderWithProducts = new OrderResponseDto(null, updatedDate, updatedOrderItemList);

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(orderForUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseDto.class)
                .value(order -> assertOrderEquals(updatedOrderWithProducts, order));
    }

    @Test
    void shouldThrowExceptionUpdatedOrderIdDoesNotExist() {

        var orderForUpdate = new OrderRequestDto(LocalDate.now(), orderItems.subList(0, 1));

        webTestClient.put().uri(URI.concat("/{id}"), NOT_EXISTED_ORDER_ID)
                .bodyValue(orderForUpdate)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldThrowExceptionUpdatedOrderIfProductHasDuplicate() {

        var savedOrderWithProducts = createOrdersWithProducts(3).get(0);
        Long id = savedOrderWithProducts.getId();

        List<OrderItemRequestDto> orderItemsWithDuplicate = List.of(orderItems.get(0), orderItems.get(0));
        var orderForUpdate = new OrderRequestDto(LocalDate.now(), orderItemsWithDuplicate);

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(orderForUpdate)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldThrowExceptionUpdatedOrderIfProductQuantityBelowZero() {

        var savedOrderWithProducts = createOrdersWithProducts(3).get(0);
        Long id = savedOrderWithProducts.getId();

        List<OrderItemRequestDto> orderItemsWithQuantityBelowZero = List.of(new OrderItemRequestDto(1L, -10));
        var orderForUpdate = new OrderRequestDto(LocalDate.now(), orderItemsWithQuantityBelowZero);

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(orderForUpdate)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldThrowExceptionUpdatedOrderIfProductDoesNotExist() {

        var savedOrderWithProducts = createOrdersWithProducts(3).get(0);
        Long id = savedOrderWithProducts.getId();

        List<OrderItemRequestDto> orderItemsWithNotExistingProduct = List.of(new OrderItemRequestDto(NOT_EXISTED_PRODUCT_ID, 100));
        var orderForUpdate = new OrderRequestDto(LocalDate.now(), orderItemsWithNotExistingProduct);

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(orderForUpdate)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    private void saveProductsIfNotExist(List<Product> productList) {
        Flux.fromIterable(productList)
                .map(p -> new Product(null, p.getName(), p.getDescription(), p.getPrice()))
                .flatMap(p -> productRepository.findByName(p.getName())
                        .switchIfEmpty(Mono.defer(() -> productRepository.save(p))))
                .subscribe();
    }

    private @NotNull List<OrderResponseDto> createOrdersWithProducts(int num) {
        List<OrderResponseDto> orders = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            var savedOrderWithProducts = orderService.create(USER_ID)
                    .flatMap(o -> orderService.addProductByOrderId(o.getId(), orderItems.get(0))
                            .then(orderService.addProductByOrderId(o.getId(), orderItems.get(1)))
                            .then(orderService.addProductByOrderId(o.getId(), orderItems.get(2))))
                    .block();
            orders.add(savedOrderWithProducts);
        }
        return orders;
    }

    private void assertOrderListEquals(List<OrderResponseDto> expectedOrders, @NotNull List<OrderResponseDto> actualOrders) {
        assertEquals(expectedOrders.size(), actualOrders.size());
        IntStream.range(0, actualOrders.size())
                .forEach(i -> assertOrderEquals(expectedOrders.get(i), actualOrders.get(i)));
    }

    private void assertOrderEquals(@NotNull OrderResponseDto expectedOrder, @NotNull OrderResponseDto actualOrder) {
        assertEquals(expectedOrder.getDate(), actualOrder.getDate());
        assertOrderItemListEquals(expectedOrder.getOrderItems(), actualOrder.getOrderItems());
    }

    private void assertOrderItemListEquals(List<OrderItemWithProductResponseDto> expectedOrderItems, List<OrderItemWithProductResponseDto> actualOrderItems) {
        if (actualOrderItems == null) {
            assertNull(expectedOrderItems);
        } else {
            assertEquals(expectedOrderItems.size(), actualOrderItems.size());
            IntStream.range(0, actualOrderItems.size())
                    .forEach(i -> assertOrderItemEquals(expectedOrderItems.get(i), actualOrderItems.get(i)));
        }
    }

    private void assertOrderItemEquals(@NotNull OrderItemWithProductResponseDto expectedOrderItem, @NotNull OrderItemWithProductResponseDto actualOrderItem) {
        assertEquals(expectedOrderItem.getProduct(), actualOrderItem.getProduct());
        assertEquals(expectedOrderItem.getQuantity(), actualOrderItem.getQuantity());
    }

}
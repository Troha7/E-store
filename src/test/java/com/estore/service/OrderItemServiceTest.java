package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.dto.response.OrderItemWithProductResponseDto;
import com.estore.model.OrderItem;
import com.estore.model.Product;
import com.estore.repository.OrderItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;

/**
 * The class {@link OrderItemServiceTest} implements unit tests of service methods
 * that use {@link OrderItemRepository} to work with {@link OrderItem} class objects.
 *
 * @author Dmytro Trotsenko on 4/10/23
 */

@SpringBootTest
@Slf4j
public class OrderItemServiceTest {

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderItemRepository orderItemRepository;

    private final List<OrderItem> orderItemList = Arrays.asList(
            new OrderItem(),

            new OrderItem(1L, 1L, 1L, 10),
            new OrderItem(2L, 1L, 2L, 2),

            new OrderItem(3L, 2L, 1L, 1),
            new OrderItem(4L, 2L, 2L, 20)
    );

    private final List<Product> productList = Arrays.asList(
            new Product(1L, "product-1", "description-1", BigDecimal.valueOf(10.0)),
            new Product(2L, "product-2", "description-2", BigDecimal.valueOf(20.0)),
            new Product(3L, "product-3", "description-3", BigDecimal.valueOf(30.0))
    );

    @Test
    @DisplayName("Test find all OrderItems with Products by order Id")
    public void testFindAllOrderItemsWithProductsByOrderId() {
        log.info("Starting testFindAllOrderItemsWithProductsByOrderId");

        Long orderId = 1L;
        List<OrderItem> filteredOrderItems = filterOrderItemsByOrderId(orderId);

        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.fromIterable(filteredOrderItems));
        when(orderItemRepository.findProductsByOrderId(orderId)).thenReturn(Flux.fromIterable(productList));

        List<OrderItemWithProductResponseDto> orderItemsWithProduct = IntStream.range(0, filteredOrderItems.size())
                .mapToObj(i -> {
                    var orderItemWithProduct = objectMapper.convertValue
                            (filteredOrderItems.get(i), OrderItemWithProductResponseDto.class);
                    orderItemWithProduct.setProduct(productList.get(i));
                    return orderItemWithProduct;
                })
                .toList();

        StepVerifier.create(orderItemService.findAllOrderItemsWithProductsByOrderId(orderId))
                .expectNextSequence(orderItemsWithProduct)
                .verifyComplete();

        log.info("Test testFindAllOrderItemsWithProductsByOrderId completed.");
    }

    @Test
    @DisplayName("Test find all OrderItems with Products by order Id when Order dos not exist")
    public void testFindAllOrderItemsWithProductsByOrderIdWhenOrderNotExist() {
        log.info("Starting testFindAllOrderItemsWithProductsByOrderIdWhenOrderNotExist");

        Long orderId = 100L;

        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.empty());
        when(orderItemRepository.findProductsByOrderId(orderId)).thenReturn(Flux.empty());

        StepVerifier.create(orderItemService.findAllOrderItemsWithProductsByOrderId(orderId))
                .verifyComplete();

        log.info("Test testFindAllOrderItemsWithProductsByOrderIdWhenOrderNotExist completed.");
    }

    @Test
    @DisplayName("Test add Product by orderId")
    public void testAddProductByOrderId() {
        log.info("Starting testAddProductByOrderId");

        Long orderId = 1L;
        var addedOrderItem = new OrderItem(null, orderId, 3L, 10);

        List<OrderItem> filteredOrderItems = filterOrderItemsByOrderId(orderId);

        when(orderItemRepository.existByOrderIdAndProductId(orderId, addedOrderItem.getProductId())).thenReturn(Mono.just(Boolean.TRUE));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.fromIterable(filteredOrderItems));
        when(orderItemRepository.save(addedOrderItem)).thenReturn(Mono.just(addedOrderItem));

        StepVerifier.create(orderItemService.addProductByOrderId(orderId, objectMapper.convertValue(addedOrderItem, OrderItemRequestDto.class)))
                .expectNext(objectMapper.convertValue(addedOrderItem, OrderItemResponseDto.class))
                .verifyComplete();

        log.info("Test testAddProductByOrderId completed.");
    }

    @Test
    @DisplayName("Test update when Product id exist")
    public void testAddProductByOrderIdUpdateWhenProductIDExist() {
        log.info("testAddProductByOrderIdUpdateWhenProductIDExist");

        Long orderId = 1L;
        var addedOrderItem = new OrderItemRequestDto(2L, 10);

        List<OrderItem> filteredOrderItems = filterOrderItemsByOrderId(orderId);

        OrderItem savedOrderItem = filteredOrderItems.stream()
                .filter(o -> o.getProductId().equals(addedOrderItem.getProductId()))
                .map(o -> {
                    OrderItem orderItem = objectMapper.convertValue(addedOrderItem, OrderItem.class);
                    orderItem.setOrderId(orderId);
                    orderItem.setId(o.getId());
                    orderItem.setQuantity(o.getQuantity() + orderItem.getQuantity());
                    return orderItem;
                })
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);

        when(orderItemRepository.existByOrderIdAndProductId(orderId, addedOrderItem.getProductId())).thenReturn(Mono.just(Boolean.TRUE));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.fromIterable(filteredOrderItems));
        when(orderItemRepository.save(savedOrderItem)).thenReturn(Mono.just(savedOrderItem));

        StepVerifier.create(orderItemService.addProductByOrderId(orderId, addedOrderItem))
                .expectNext(objectMapper.convertValue(savedOrderItem, OrderItemResponseDto.class))
                .verifyComplete();

        log.info("Test testAddProductByOrderIdUpdateWhenProductIDExist completed.");
    }

    @Test
    @DisplayName("Test add Product by orderId if Order dos not exist")
    public void testAddProductByOrderIdOrderNotExist() {
        log.info("Starting testAddProductByOrderIdOrderNotExist");

        Long orderId = 100L;
        var addedOrderItem = new OrderItem(null, orderId, 3L, 10);

        List<OrderItem> filteredOrderItems = filterOrderItemsByOrderId(orderId);

        when(orderItemRepository.existByOrderIdAndProductId(orderId, addedOrderItem.getProductId())).thenReturn(Mono.just(Boolean.FALSE));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.fromIterable(filteredOrderItems));
        when(orderItemRepository.save(addedOrderItem)).thenReturn(Mono.just(addedOrderItem));

        StepVerifier.create(orderItemService.addProductByOrderId(orderId, objectMapper.convertValue(addedOrderItem, OrderItemRequestDto.class)))
                        .expectError(EntityNotFoundException.class)
                                .verify();

        log.info("Test testAddProductByOrderIdOrderNotExist completed.");
    }

    @Test
    @DisplayName("Test add Product by orderId if Product dos not exist")
    public void testAddProductByOrderIdProductNotExist() {
        log.info("Starting testAddProductByOrderIdProductNotExist");

        Long orderId = 1L;
        var addedOrderItem = new OrderItem(null, orderId, 3L, 10);

        List<OrderItem> filteredOrderItems = filterOrderItemsByOrderId(orderId);

        when(orderItemRepository.existByOrderIdAndProductId(orderId, addedOrderItem.getProductId())).thenReturn(Mono.just(Boolean.FALSE));
        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(Flux.fromIterable(filteredOrderItems));
        when(orderItemRepository.save(addedOrderItem)).thenReturn(Mono.just(addedOrderItem));

        StepVerifier.create(orderItemService.addProductByOrderId(orderId, objectMapper.convertValue(addedOrderItem, OrderItemRequestDto.class)))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testAddProductByOrderIdProductNotExist completed.");
    }

    private List<OrderItem> filterOrderItemsByOrderId(Long orderId) {
        return orderItemList.stream()
                .skip(1)
                .filter(o -> o.getOrderId().equals(orderId))
                .toList();
    }

}

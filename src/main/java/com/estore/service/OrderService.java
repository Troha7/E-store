package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.model.Order;
import com.estore.model.OrderItem;
import com.estore.model.Product;
import com.estore.repository.OrderItemRepository;
import com.estore.repository.OrderRepository;
import com.estore.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

/**
 * {@link OrderService}
 *
 * @author Dmytro Trotsenko on 3/18/23
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new empty Order
     *
     * @return the saved order without the related entities
     */
    @Transactional
    public Mono<OrderResponseDto> create() {
        log.info("Start to create order");
        Order order = objectMapper.convertValue(new OrderRequestDto(LocalDate.now(), null), Order.class);
        return orderRepository.save(order)
                .map(o -> objectMapper.convertValue(o, OrderResponseDto.class))
                .doOnSuccess(o -> log.info("Order id={} have been created", o.getId()));
    }

    /**
     * Update an Order
     *
     * @param id              order id
     * @param orderRequestDto order to be saved
     * @return the saved order with the related products
     */
    @Transactional
    public Mono<OrderResponseDto> update(Long id, OrderRequestDto orderRequestDto) {
        log.info("Start to update order id={}", id);
        List<OrderItemRequestDto> orderItemDtos = orderRequestDto.getProducts();

        // Check the existing duplicates productId
        return checkDuplicateProductId(orderItemDtos)

                // Find the existing links to the Products
                .then(findAllOrderItemsByOrderId(id).collectList())

                // Remove and add the links to the products
                .flatMap(currentOrderItems ->
                        // Delete all Order Items which will not be updated
                        orderItemRepository.deleteAll(getRemovedOrderItems(orderItemDtos, currentOrderItems))
                                // Insert all Order Items which will be updated
                                .then(orderItemRepository.saveAll(getAddedOrderItems(id, orderItemDtos, currentOrderItems))
                                        .collectList()))

                // Save the Order
                .then(saveOrderById(id, orderRequestDto))

                // Return Updated Order
                .then(findById(id))
                .doOnSuccess(o -> log.info("Order have been updated"));
    }

    /**
     * Add product and quantity an Order by order id
     *
     * @param orderId             order id
     * @param orderItemRequestDto order item to be saved
     * @return the saved order item without the related products
     */
    @Transactional
    public Mono<OrderItemResponseDto> addProduct(Long orderId, OrderItemRequestDto orderItemRequestDto) {
        log.info("Start to addProduct {}", orderItemRequestDto);
        return findAllOrderItemsByOrderId(orderId)
                .filter(o -> o.getProductId().equals(orderItemRequestDto.getProductId()))
                .last(new OrderItem())
                .map(o -> {
                    if (o.getId() == null) {
                        OrderItem orderItem = objectMapper.convertValue(orderItemRequestDto, OrderItem.class);
                        orderItem.setOrderId(orderId);
                        log.info("Add new Product");
                        return orderItem;
                    } else {
                        o.setQuantity(o.getQuantity() + orderItemRequestDto.getQuantity());
                        log.info("Update Product and summarizing quantity");
                        return o;
                    }
                })
                .flatMap(orderItemRepository::save)
                .map(o -> objectMapper.convertValue(o, OrderItemResponseDto.class))
                .doOnSuccess(o -> log.info("Product have been added"));
    }

    /**
     * Find Order by id
     *
     * @param id order id
     * @return Find order with the related products loaded
     * @throws EntityNotFoundException Order with id wasn't found
     */
    public Mono<OrderResponseDto> findById(Long id) {
        log.info("Start to find order by id={}", id);
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Order id=" + id + " wasn't found")))
                .doOnError(o -> log.warn("Order id=" + id + " wasn't found"))
                .flatMap(this::loadOrderRelations)
                .map(o -> objectMapper.convertValue(o, OrderResponseDto.class))
                .doOnSuccess(o -> log.info("Order id={} have been found", o.getId()));
    }

    /**
     * Find all Orders
     *
     * @return Find all orders with the related products loaded
     */
    public Flux<OrderResponseDto> findAll() {
        log.info("Start to find all orders");
        return orderRepository.findAll()
                .flatMap(this::loadOrderRelations)
                .map(o -> objectMapper.convertValue(o, OrderResponseDto.class))
                .doOnSubscribe(o -> log.info("All orders have been found"));
    }

    /**
     * Delete Order by id
     *
     * @param id order id
     */
    @Transactional
    public Mono<Void> deleteById(Long id) {
        log.info("Start to delete order by id={}", id);
        return findById(id)
                .map(o -> objectMapper.convertValue(o, Order.class))
                .flatMap(orderRepository::delete)
                .then(orderItemRepository.deleteAllByOrderId(id))
                .doOnSuccess(o -> log.info("Order id={} have been deleted", id));
    }

    /**
     * Load the products related to an order
     *
     * @param order Order
     * @return The order with the loaded related products
     */
    private Mono<Order> loadOrderRelations(Order order) {

        Mono<List<Product>> products = productRepository
                .findProductsByOrderId(order.getId())
                .collectList();

        return Mono.just(order)
                .zipWith(products)
                .map(result -> {
                    result.getT1().setProducts(result.getT2());
                    return result.getT1();
                });
    }

    /**
     * Save only unique Order by id
     *
     * @param id              order id
     * @param orderRequestDto order to be saved
     * @return the saved order
     */
    private Mono<Order> saveOrderById(Long id, OrderRequestDto orderRequestDto) {
        return Mono.just(orderRequestDto)
                .map(o -> {
                    Order order = objectMapper.convertValue(o, Order.class);
                    order.setId(id);
                    return order;
                })
                .filterWhen(updatedOrder -> orderRepository.findById(id)
                        .filter(o -> !o.getDate().equals(updatedOrder.getDate()))
                        .hasElement())
                .flatMap(orderRepository::save);
    }

    /**
     * Find all order items by Order id
     *
     * @param id order id
     * @return Find all order items
     */
    private Flux<OrderItem> findAllOrderItemsByOrderId(Long id) {
        return orderItemRepository.findAllByOrderId(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException
                        ("OrderItems with Order id=" + id + " wasn't found")))
                .doOnError(o -> log.warn("OrderItems with Order id=" + id + " wasn't found"));
    }

    /**
     * Check haven't order Item Dtos duplicate ProductId
     *
     * @param orderItemDtos list of order Item Dtos
     * @return Trow the {@link IllegalArgumentException} when list of order Item Dtos have the duplicate
     * @throws IllegalArgumentException Order have duplicate productId
     */
    private static Flux<Object> checkDuplicateProductId(List<OrderItemRequestDto> orderItemDtos) {
        return Flux.fromIterable(orderItemDtos)
                .filter(o -> orderItemDtos.stream()
                        .filter(p -> p.getProductId().equals(o.getProductId()))
                        .count() > 1)
                .flatMap(o -> {
                    log.warn("Order have duplicate productId={}", o.getProductId());
                    return Mono.error(new IllegalArgumentException
                            ("Order have duplicate productId=" + o.getProductId()));
                });
    }

    /**
     * Get list of OrderItems for add relations entities from Order
     *
     * @param id                order id
     * @param orderItems        updated OrderItems
     * @param currentOrderItems current OrderItems
     * @return list for add OrderItems from repository
     */
    private static List<OrderItem> getAddedOrderItems(Long id, List<OrderItemRequestDto> orderItems, List<OrderItem> currentOrderItems) {
        return IntStream.range(0, orderItems.size())
                .mapToObj(i -> {
                    OrderItem orderItem = new OrderItem();
                    if (currentOrderItems.size() > i) {
                        orderItem.setId(currentOrderItems.get(i).getId());
                    }
                    orderItem.setOrderId(id);
                    orderItem.setProductId(orderItems.get(i).getProductId());
                    orderItem.setQuantity(orderItems.get(i).getQuantity());
                    return orderItem;
                })
                .filter(addedOrderItem -> !currentOrderItems.contains(addedOrderItem))
                .toList();
    }

    /**
     * Get list of OrderItems for remove relations entities from Order
     *
     * @param orderItems        updated OrderItems
     * @param currentOrderItems current OrderItems
     * @return list for remove OrderItems from repository
     */
    private static List<OrderItem> getRemovedOrderItems(List<OrderItemRequestDto> orderItems, List<OrderItem> currentOrderItems) {
        List<Long> prodIds = orderItems.stream()
                .map(OrderItemRequestDto::getProductId)
                .toList();

        return currentOrderItems.stream()
                .filter(currOrderItem -> !prodIds.contains(currOrderItem.getProductId()))
                .toList();
    }

}

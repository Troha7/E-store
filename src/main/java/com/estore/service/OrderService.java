package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.mapper.OrderMapper;
import com.estore.model.Order;
import com.estore.model.OrderItem;
import com.estore.repository.OrderItemRepository;
import com.estore.repository.OrderRepository;
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
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final OrderMapper orderMapper;

    /**
     * Create a new empty Order
     *
     * @return the saved order without the related entities
     */
    @Transactional
    public Mono<OrderResponseDto> create(Long userId) {
        log.info("Start to create order");
        return orderRepository.save(new Order(null, userId, LocalDate.now()))
                .map(orderMapper::toDto)
                .doOnSuccess(o -> log.info("Order id={} have been created", o.getId()));
    }

    /**
     * Add product and quantity in Order by order id
     *
     * @param id                  order id
     * @param orderItemRequestDto order item to be saved
     * @return the saved order item with related products
     */

    public Mono<OrderResponseDto> addProductByOrderId(Long id, OrderItemRequestDto orderItemRequestDto) {
        return orderItemService.addProductByOrderId(id, orderItemRequestDto)
                .then(findById(id));
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
                .doOnSubscribe(o -> log.info("All orders have been found"));
    }

    /**
     * Deletes order by id.
     * Also deletes all related order items.
     *
     * @param id Order id.
     * @return Mono<Void>
     * @throws EntityNotFoundException if the order is not found.
     */
    @Transactional
    public Mono<Void> deleteById(Long id) {
        log.info("Start to delete order by id={}", id);
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Order id=" + id + " wasn't found")))
                .flatMap(orderRepository::delete)
                .doOnSuccess(o -> log.info("Order id={} has been deleted", id));
    }

    /**
     * Deletes all Orders.
     * Also deletes all related order items.
     *
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteAll() {
        log.info("Start to delete all Orders");
        return orderRepository.deleteAll()
                .doOnSuccess(o -> log.info("All Orders has been deleted"));
    }

    /**
     * Updates an existing order with new order items.
     *
     * @param id              Order id.
     * @param orderRequestDto the new order with OrderItem list.
     * @return Updated order and its related order items.
     */
    @Transactional
    public Mono<OrderResponseDto> update(Long id, OrderRequestDto orderRequestDto) {
        log.info("Start to update Order id={}", id);
        List<OrderItemRequestDto> orderItemDtos = orderRequestDto.getProducts();

        return existsOrderById(id)
                .then(existsProductsInList(orderItemDtos))

                // Find the existing links to the Products
                .then(orderItemRepository.findAllByOrderId(id).collectList())
                .flatMap(currentOrderItems ->

                        // Delete all Order Items which will not be updated
                        orderItemRepository.deleteAll(getRemovedOrderItems(orderItemDtos, currentOrderItems))

                                // Insert all Order Items which will be updated
                                .thenMany(orderItemRepository.saveAll(getAddedOrderItems(id, orderItemDtos, currentOrderItems)))

                                // Update the Order
                                .then(saveOrderById(id, orderRequestDto))
                                .flatMap(this::loadOrderRelations)
                )
                .doOnSuccess(o -> log.info("Order has been updated"));
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    /**
     * Load the products related to an order
     *
     * @param order Order
     * @return The order with the loaded related products
     */
    private Mono<OrderResponseDto> loadOrderRelations(Order order) {
        return orderItemService.findAllOrderItemsWithProductsByOrderId(order.getId()).collectList()
                .map(orderItems -> {
                    OrderResponseDto orderWithProducts = orderMapper.toDto(order);
                    orderWithProducts.setOrderItems(orderItems);
                    return orderWithProducts;
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
        return Mono.just(orderMapper.toModel(orderRequestDto))
                .doOnNext(order -> order.setId(id))
                .flatMap(orderRepository::save);
    }

    /**
     * Get list of OrderItems for add relations entities from Order
     *
     * @param id                order id
     * @param orderItems        updated OrderItems
     * @param currentOrderItems current OrderItems
     * @return list for add OrderItems from repository
     */
    private List<OrderItem> getAddedOrderItems(Long id, List<OrderItemRequestDto> orderItems, List<OrderItem> currentOrderItems) {
        List<OrderItem> addedOrderItems = IntStream.range(0, orderItems.size())
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

        log.info("OrderItems list for updating {}", addedOrderItems);
        return addedOrderItems;
    }

    /**
     * Get list of OrderItems for remove relations entities from Order
     *
     * @param orderItems        updated OrderItems
     * @param currentOrderItems current OrderItems
     * @return list for remove OrderItems from repository
     */
    private static List<OrderItem> getRemovedOrderItems(List<OrderItemRequestDto> orderItems, List<OrderItem> currentOrderItems) {
        int skippedOrderItems = Math.min(currentOrderItems.size(), orderItems.size());

        List<OrderItem> removedOrderItems = currentOrderItems.stream()
                .skip(skippedOrderItems)
                .toList();

        log.info("OrderItems list for deleting {}", removedOrderItems);
        return removedOrderItems;
    }

    /**
     * Checks if an Order exists in the repository.
     *
     * @param id Order id.
     * @return True if the order exists.
     * @throws IllegalArgumentException If the Order id is null or not found in the repository.
     */
    private Mono<Boolean> existsOrderById(Long id) {
        return orderRepository.existsById(id)
                .filter(exist -> exist)
                .switchIfEmpty(Mono.error(new IllegalArgumentException
                        ("Order id=" + id + " does not exist in repository")));
    }

    /**
     * Checks if all the product IDs in the order exist in the repository.
     *
     * @param orderItemDtos List of OrderItemRequestDto.
     * @return True if all the product IDs exist.
     * @throws IllegalArgumentException If any of the product IDs in the Order are null or not found in the repository.
     */
    private Mono<Boolean> existsProductsInList(List<OrderItemRequestDto> orderItemDtos) {
        List<Long> productIds = orderItemDtos.stream()
                .map(OrderItemRequestDto::getProductId)
                .toList();
        return productService.existsProductByIdIn(productIds)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new IllegalArgumentException
                        ("Some Product ides: " + productIds + " does not exist in repository")));
    }

}

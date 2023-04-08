package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.dto.response.OrderWithProductsResponseDto;
import com.estore.model.Order;
import com.estore.model.OrderItem;
import com.estore.repository.OrderItemRepository;
import com.estore.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final ObjectMapper objectMapper;

    /**
     * Create a new empty Order
     *
     * @return the saved order without the related entities
     */
    @Transactional
    public Mono<OrderResponseDto> create() {
        log.info("Start to create order");
        return orderRepository.save(new Order(null, null, LocalDate.now()))
                .map(o -> objectMapper.convertValue(o, OrderResponseDto.class))
                .doOnSuccess(o -> log.info("Order id={} have been created", o.getId()));
    }

    /**
     * Add product and quantity an Order by order id
     *
     * @param id                  order id
     * @param orderItemRequestDto order item to be saved
     * @return the saved order item without the related products
     */

    public Mono<OrderItemResponseDto> addProductByOrderId(Long id, OrderItemRequestDto orderItemRequestDto) {
        return orderItemService.addProductByOrderId(id, orderItemRequestDto);
    }

    /**
     * Find Order by id
     *
     * @param id order id
     * @return Find order with the related products loaded
     * @throws EntityNotFoundException Order with id wasn't found
     */
    public Mono<OrderWithProductsResponseDto> findById(Long id) {
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
    public Flux<OrderWithProductsResponseDto> findAll() {
        log.info("Start to find all orders");
        return orderRepository.findAll()
                .flatMap(this::loadOrderRelations)
                .doOnSubscribe(o -> log.info("All orders have been found"));
    }

    /**
     * Deletes an order by id.
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
                .flatMap(o -> Mono.zip(Mono.just(o), orderItemRepository.deleteAllByOrderId(id)).thenReturn(o))
                .flatMap(orderRepository::delete)
                .doOnSuccess(o -> log.info("Order id={} has been deleted", id));
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
        List<OrderItemRequestDto> orderItemDtos = orderRequestDto.getProducts();

        // Validate OrderItems
        validateOrderItems(orderItemDtos);

        return existsOrderById(id)
                .then(existsProductsInList(orderItemDtos))

                // Find the existing links to the Products
                .then(getCurrentOrderItems(id))
                .flatMap(currentOrderItems ->

                        // Delete all Order Items which will not be updated
                        orderItemRepository.deleteAll(getRemovedOrderItems(orderItemDtos, currentOrderItems))

                                // Insert all Order Items which will be updated
                                .thenMany(orderItemRepository.saveAll(getAddedOrderItems(id, orderItemDtos, currentOrderItems)))
                                .map(o -> objectMapper.convertValue(o, OrderItemResponseDto.class))
                                .collectList()

                                // Update the Order
                                .zipWith(saveOrderById(id, orderRequestDto))
                                .map(res -> {
                                    OrderResponseDto orderResponseDto = objectMapper.convertValue(res.getT2(), OrderResponseDto.class);
                                    orderResponseDto.setOrderItems(res.getT1());
                                    return orderResponseDto;
                                })
                );
    }

    private Mono<List<OrderItem>> getCurrentOrderItems(Long id) {
        return orderItemRepository.findAllByOrderId(id).collectList();
    }


    /**
     * Load the products related to an order
     *
     * @param order Order
     * @return The order with the loaded related products
     */
    private Mono<OrderWithProductsResponseDto> loadOrderRelations(Order order) {
        return orderItemService.findAllProductsByOrderId(order.getId()).collectList()
                .map(orderItems -> {
                    OrderWithProductsResponseDto orderWithProducts = objectMapper.convertValue(order, OrderWithProductsResponseDto.class);
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
        return Mono.just(orderRequestDto)
                .map(o -> {
                    Order order = objectMapper.convertValue(o, Order.class);
                    order.setId(id);
                    return order;
                })
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

    /**
     * Validates the order items, checks if there are no duplicate product IDs
     * and if the quantity of each item is greater than 0.
     *
     * @param orderItemDtos List of OrderItemRequestDto.
     * @throws IllegalArgumentException If there are duplicate product IDs or
     *                                  the quantity of any item is less than or equal to 0.
     */
    private void validateOrderItems(List<OrderItemRequestDto> orderItemDtos) {
        Set<Long> productIds = new HashSet<>();

        for (OrderItemRequestDto orderItemDto : orderItemDtos) {
            if (!productIds.add(orderItemDto.getProductId())) {
                throw new IllegalArgumentException("Order has duplicate productId=" + orderItemDto.getProductId());
            }
            if (orderItemDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity <= 0");
            }
        }
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

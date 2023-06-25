package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.exception.ModelNotFoundException;
import com.estore.mapper.OrderItemMapper;
import com.estore.model.OrderItem;
import com.estore.repository.OrderItemRepository;
import com.estore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * {@link OrderItemService}
 *
 * @author Dmytro Trotsenko on 4/3/23
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    private final ProductRepository productRepository;

    private final OrderItemMapper orderItemMapper;

    /**
     * Finds all OrderItems and Products by Order id.
     *
     * @param id Order id.
     * @return OrderItemWithProductResponseDto objects containing order item and product information
     */

    public Flux<OrderItemResponseDto> findAllOrderItemsWithProductsByOrderId(Long id) {
        return orderItemRepository.findAllByOrderId(id)
                .flatMap(orderItem -> productRepository.findProductsByOrderId(id)
                        .filter(product -> orderItem.getProductId().equals(product.getId()))
                        .map(product -> {
                            var orderItemDto = orderItemMapper.toDto(orderItem);
                            orderItemDto.setProduct(product);
                            return orderItemDto;
                        }));
    }

    /**
     * Add a product to the order by order id.
     *
     * @param orderId             Order id.
     * @param orderItemRequestDto The product information for add to the order.
     * @return Added OrderItemResponseDto.
     */
    @Transactional
    public Mono<OrderItemResponseDto> addProductByOrderId(Long orderId, OrderItemRequestDto orderItemRequestDto) {
        log.info("Start to addProduct {}", orderItemRequestDto);

        return checkExistOrderAndProduct(orderId, orderItemRequestDto.getProductId())
                .then(orderItemRepository.findAllByOrderId(orderId)
                        .filter(orderItem -> Objects.equals(orderItem.getProductId(), orderItemRequestDto.getProductId()))
                        .last(new OrderItem())
                        .flatMap(existingOrderItem -> {
                            OrderItem orderItem = orderItemMapper.toModel(orderItemRequestDto);
                            orderItem.setOrderId(orderId);

                            //Updating product and summarizing quantity
                            if (existingOrderItem.getProductId() != null) {
                                orderItem.setId(existingOrderItem.getId());
                                orderItem.setQuantity(existingOrderItem.getQuantity() + orderItemRequestDto.getQuantity());
                                log.info("Updating product and summarizing quantity existingQuantity={} + addedQuantity={}",
                                        existingOrderItem.getQuantity(), orderItemRequestDto.getQuantity());
                            }

                            return orderItemRepository.save(orderItem)
                                    .flatMap(savedOrderItem -> productRepository.findById(savedOrderItem.getProductId())
                                            .map(product -> {
                                                var orderItemWithProduct = orderItemMapper.toDto(savedOrderItem);
                                                orderItemWithProduct.setProduct(product);
                                                return orderItemWithProduct;
                                            }))
                                    .doOnSuccess(savedOrderItem -> log.info("Product has been added"));
                        }));
    }

    /**
     * Remove a product from the order by order id and product id.
     *
     * @param orderId order id
     * @param productId product id
     */
    @Transactional
    public Mono<Void> removeProductFromOrderById(Long orderId, Long productId) {
        log.info("Start to remove Product id={} from Order id={}", productId, orderId);
        return checkExistOrderAndProduct(orderId, productId)
                .then(orderItemRepository.deleteOrderItemByOrderIdAndProductId(orderId,productId))
                .doOnSuccess(res -> log.info("Product id={} has been removed from Order id={}", productId, orderId));
    }

    /**
     * Checks if the given order and product exist.
     *
     * @param orderId   Order id.
     * @param productId Product id.
     * @return Mono<Boolean> true if existed Order and Product in repository.
     * @throws ModelNotFoundException If the order or product is not found.
     */

    public Mono<Boolean> checkExistOrderAndProduct(Long orderId, Long productId) {
        return orderItemRepository.existByOrderIdAndProductId(orderId, productId)
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Order or Product not found")))
                .doOnError(error -> log.info("Order or Product not found"));
    }

}

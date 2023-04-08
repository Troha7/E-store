package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.dto.response.OrderItemWithProductResponseDto;
import com.estore.model.OrderItem;
import com.estore.repository.OrderItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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

    private final ObjectMapper objectMapper;

    /**
     * Finds all OrderItems and Products by Order id.
     *
     * @param id Order id.
     * @return OrderItemWithProductResponseDto objects containing order item and product information
     */

    public Flux<OrderItemWithProductResponseDto> findAllProductsByOrderId(Long id) {

        return orderItemRepository.findAllByOrderId(id)
                .map(o -> objectMapper.convertValue(o, OrderItemWithProductResponseDto.class))
                .zipWith(orderItemRepository.findProductsByOrderId(id))
                .map(result -> {
                    result.getT1().setProduct(result.getT2());
                    return result.getT1();
                });
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
        // Check that the quantity is valid.
        if (orderItemRequestDto.getQuantity() <= 0) {
            log.warn("Quantity <= 0");
            return Mono.error(new IllegalArgumentException("Quantity must be greater than 0"));
        }

        return checkExistOrderAndProduct(orderId, orderItemRequestDto)
                .then(orderItemRepository.findAllByOrderId(orderId)
                        .filter(orderItem -> Objects.equals(orderItem.getProductId(), orderItemRequestDto.getProductId()))
                        .last(new OrderItem())
                        .flatMap(existingOrderItem -> {
                            OrderItem orderItem = objectMapper.convertValue(orderItemRequestDto, OrderItem.class);
                            orderItem.setOrderId(orderId);

                            //Updating product and summarizing quantity
                            if (existingOrderItem.getProductId() != null) {
                                orderItem.setId(existingOrderItem.getId());
                                orderItem.setQuantity(existingOrderItem.getQuantity() + orderItemRequestDto.getQuantity());
                                log.info("Updating product and summarizing quantity");
                            }

                            return orderItemRepository
                                    .save(orderItem)
                                    .map(savedOrderItem -> objectMapper.convertValue(savedOrderItem, OrderItemResponseDto.class))
                                    .doOnSuccess(savedOrderItem -> log.info("Product has been added"));
                        }));
    }

    /**
     * Checks if the given order and product exist.
     *
     * @param orderId             Order id.
     * @param orderItemRequestDto The OrderItem information to check.
     * @return OrderItemRequestDto if existed Order and Product in repository.
     * @throws EntityNotFoundException If the order or product is not found.
     */
    public Mono<OrderItemRequestDto> checkExistOrderAndProduct(Long orderId, OrderItemRequestDto orderItemRequestDto) {
        return orderItemRepository.existByOrderIdAndProductId(orderId, orderItemRequestDto.getProductId())
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Order or Product not found")))
                .thenReturn(orderItemRequestDto)
                .doOnError(error -> log.warn("Order or Product not found"));
    }

}

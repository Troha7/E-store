package com.estore.service;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.model.OrderItem;
import com.estore.repository.OrderItemRepository;
import com.estore.repository.ProductRepository;
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

    private final ProductRepository productRepository;

    private final ObjectMapper objectMapper;

    /**
     * Finds all OrderItems and Products by Order id.
     *
     * @param id Order id.
     * @return OrderItemWithProductResponseDto objects containing order item and product information
     */

    public Flux<OrderItemResponseDto> findAllOrderItemsWithProductsByOrderId(Long id) {

        return orderItemRepository.findAllByOrderId(id)
                .map(o -> objectMapper.convertValue(o, OrderItemResponseDto.class))
                .zipWith(productRepository.findProductsByOrderId(id))
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
                                log.info("Updating product and summarizing quantity existingQuantity={} + addedQuantity={}",
                                        existingOrderItem.getQuantity(), orderItemRequestDto.getQuantity());
                            }

                            return orderItemRepository.save(orderItem)
                                    .flatMap(savedOrderItem -> productRepository.findById(savedOrderItem.getProductId())
                                            .map(product -> {
                                                var orderItemWithProduct = objectMapper.convertValue(savedOrderItem, OrderItemResponseDto.class);
                                                orderItemWithProduct.setProduct(product);
                                                return orderItemWithProduct;
                                            }))
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

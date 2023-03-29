package com.estore.controller;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link OrderController}
 *
 * @author Dmytro Trotsenko on 3/18/23
 */

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponseDto> createOrder() {
        return orderService.create();
    }

    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderItemResponseDto> addProduct(@PathVariable long orderId, @RequestBody OrderItemRequestDto orderItem) {
        return orderService.addProduct(orderId, orderItem);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderResponseDto> update(@PathVariable long id, @RequestBody OrderRequestDto order) {
        return orderService.update(id, order);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<OrderResponseDto> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<OrderResponseDto> findById(@PathVariable("id") long id) {
        return orderService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable("id") long id) {
        return orderService.deleteById(id);
    }
}

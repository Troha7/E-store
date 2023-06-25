package com.estore.controller.rest;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link OrderRestController}
 *
 * @author Dmytro Trotsenko on 3/18/23
 */

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
@Slf4j
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Order")
    public Mono<OrderResponseDto> createOrder(@PathVariable long userId) {
        return orderService.create(userId);
    }

    @PostMapping("/add/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add product and quantity in Order")
    public Mono<OrderResponseDto> addProduct(@PathVariable long orderId, @Validated @RequestBody OrderItemRequestDto orderItem) {
        return orderService.addProductByOrderId(orderId, orderItem);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update an existing Order")
    public Mono<OrderResponseDto> update(@PathVariable long id, @Validated @RequestBody OrderRequestDto order) {
        return orderService.update(id, order);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find all Orders")
    public Flux<OrderResponseDto> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/user/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find all Orders by UserId")
    public Flux<OrderResponseDto> findAllByUserId(@PathVariable long id) {
        return orderService.findAllByUserId(id);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find Order by id")
    public Mono<OrderResponseDto> findById(@PathVariable("id") long id) {
        return orderService.findById(id);
    }

    @DeleteMapping("/product/{userId}/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove Product from the Order by user id and product id")
    public Mono<Void> removeProduct(@PathVariable("userId") long userId, @PathVariable("productId") long productId) {
        return orderService.removeProductFromOrderById(userId, productId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Order by id")
    public Mono<Void> delete(@PathVariable("id") long id) {
        return orderService.deleteById(id);
    }

}

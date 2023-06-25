package com.estore.controller;

import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.estore.model.OrderStatus.CREATED;
import static com.estore.model.UserRole.ADMIN;

/**
 * {@link CartController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 6/14/23
 */

@Controller
@RequestMapping("cart")
@RequiredArgsConstructor
public class CartController {

    private final OrderService orderService;

    @GetMapping
    public Mono<String> getOrders(Model model) {

        return getAuthentication()
                .flatMapMany(auth -> (isAdmin(auth)) ? orderService.findAll()
                        : orderService.findAllOrderByUsernameAndStatus(auth.getName(), CREATED))
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .doOnNext(orders -> model.addAttribute("orderForm", new OrderRequestDto()))
                .doOnNext(orders -> model.addAttribute("acceptedOrder", new OrderResponseDto()))
                .map(orderDto -> "main/cart")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/cart");
                });
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderById(@PathVariable("id") Long id, Model model) {
        return orderService.findById(id)
                .flux().collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .doOnNext(orders -> model.addAttribute("orderForm", new OrderRequestDto()))
                .map(orders -> "main/cart")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/cart");
                });
    }

    @GetMapping("/user/{id}")
    public Mono<String> findAllByUserId(@PathVariable("id") Long id, Model model) {
        return orderService.findAllByUserId(id)
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .doOnNext(orders -> model.addAttribute("orderForm", new OrderRequestDto()))
                .map(orders -> "main/cart")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/cart");
                });
    }

    @PostMapping("/update/{orderId}")
    public Mono<String> update(@PathVariable("orderId") Long id, @Validated @ModelAttribute("order") OrderRequestDto order, Model model) {
        return orderService.update(id, order)
                .map(orderDto -> "redirect:/cart")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/cart");
                });
    }

    @PostMapping("/buy")
    public Mono<String> buy(@ModelAttribute("acceptedOrder") OrderResponseDto order, Model model) {
        return orderService.accept(order)
                .map(acceptedOrder -> "redirect:/cart")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/cart");
                });
    }

    private static Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(ADMIN.name()));
    }

}

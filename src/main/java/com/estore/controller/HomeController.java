package com.estore.controller;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.request.ProductRequestDto;
import com.estore.service.OrderService;
import com.estore.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static com.estore.model.OrderStatus.*;

/**
 * {@link HomeController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/16/23
 */

@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final ProductService productService;
    private final OrderService orderService;

    @GetMapping
    public Mono<String> getProducts(@RequestParam(required = false) String name, Model model) {
        var productsFlux = (name == null) ? productService.findAll() : productService.findByNameContaining(name);
        return productsFlux.collectList()
                .doOnNext(products -> model.addAttribute("products", products))
                .doOnNext(products -> model.addAttribute("product", new ProductRequestDto()))
                .doOnNext(products -> model.addAttribute("orderItem", new OrderItemRequestDto()))
                .map(products -> "main/home")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/home");
                });
    }

    @GetMapping("/{id}")
    public Mono<String> getProductById(@PathVariable("id") Long id, Model model) {
        return productService.findById(id)
                .flux().collectList()
                .doOnNext(products -> model.addAttribute("products", products))
                .map(product -> "main/home")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/home");
                });
    }

    @GetMapping("/addProduct")
    public Mono<String> addProduct(Model model) {
        model.addAttribute("productDto", new ProductRequestDto());
        return Mono.just("main/addProduct");
    }

    @PostMapping("/addProduct")
    public Mono<String> createProduct(@Validated @ModelAttribute("productDto") ProductRequestDto product, Errors errors, Model model) {
        return (errors != null && errors.hasErrors()) ? Mono.just("main/addProduct")
                : productService.create(product).then(Mono.just("redirect:/"))
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("main/addProduct");
                });
    }

    @PostMapping("/addToOrder")
    public Mono<String> addToOrder(@Validated @ModelAttribute("orderItem") OrderItemRequestDto orderItem) {
        return getAuthentication()
                .flatMapMany(auth -> orderService.findAllOrderByUsernameAndStatus(auth.getName(), CREATED))
                .last()
                .flatMap(order -> orderService.addProductByOrderId(order.getId(), orderItem))
                .then(Mono.just("redirect:/cart"));
    }

    private static Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

}

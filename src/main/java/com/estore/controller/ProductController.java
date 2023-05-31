package com.estore.controller;

import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

/**
 * {@link ProductController}
 *
 * @author Dmytro Trotsenko on 3/12/23
 */

@Controller
@RequestMapping("catalog")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String getProducts(final Model model) {
        final Flux<ProductResponseDto> all = productService.findAll();

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Product")
    public Mono<ProductResponseDto> createProduct(@Validated @RequestBody ProductRequestDto product) {
        return productService.create(product);
    }
        model.addAttribute("products", all);
        model.addAttribute("product", new ProductRequestDto());

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update an existing Product")
    public Mono<ProductResponseDto> updateProduct(@PathVariable long id, @Validated @RequestBody ProductRequestDto product) {
        return productService.update(id, product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Product by id")
    public Mono<Void> deleteProduct(@PathVariable("id") long id) {
        return productService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete all Products")
    public Mono<Void> deleteAllProducts() {
        return productService.deleteAll();
        return"main/catalog";
    }

}

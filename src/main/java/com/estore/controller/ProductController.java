package com.estore.controller;

import com.estore.dto.response.ProductResponseDto;
import com.estore.dto.request.ProductRequestDto;
import com.estore.service.ProductService;
//import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link ProductController}
 *
 * @author Dmytro Trotsenko on 3/12/23
 */

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find all Products (if not set RequestParam) or Find by containing name")
    public Flux<ProductResponseDto> getProducts(@RequestParam(required = false) String name) {
        return (name == null) ? productService.findAll() : productService.findByNameContaining(name);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find Product by id")
    public Mono<ProductResponseDto> getProductById(@PathVariable("id") long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new Product")
    public Mono<ProductResponseDto> createProduct(@RequestBody ProductRequestDto product) {
        return productService.create(product);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update an existing Product")
    public Mono<ProductResponseDto> updateProduct(@PathVariable long id, @RequestBody ProductRequestDto product) {
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
    }

}

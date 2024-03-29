package com.estore.service;

import com.estore.dto.response.ProductResponseDto;
import com.estore.dto.request.ProductRequestDto;
import com.estore.exception.ModelNotFoundException;
import com.estore.mapper.ProductMapper;
import com.estore.model.Product;
import com.estore.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * {@link ProductService}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Create new {@link Product} and save row with data in database
     *
     * @param productRequestDto object with data
     * @return ProductResponseDto
     */
    @Transactional
    public Mono<ProductResponseDto> create(ProductRequestDto productRequestDto) {
        log.info("Start to create product");
        return productRepository.findByName(productRequestDto.getName())
                .flatMap(product -> Mono.error(new ModelNotFoundException("Product name=" + product.getName() + " already exists")))
                .doOnError(ex -> log.error("Product name=" + productRequestDto.getName() + " already exists"))
                .switchIfEmpty(Mono.defer(() -> productRepository.save(productMapper.toModel(productRequestDto))
                        .map(productMapper::toDto)))
                .cast(ProductResponseDto.class)
                .doOnSuccess(dto -> log.info("Product name={} has been created", productRequestDto.getName()));
    }

    /**
     * Update {@link Product} and save row with data in database
     *
     * @param id                product id
     * @param productRequestDto object with data
     * @return ProductResponseDto
     */
    @Transactional
    public Mono<ProductResponseDto> update(Long id, ProductRequestDto productRequestDto) {
        log.info("Start to update product id={}", id);
        Product product = productMapper.toModel(productRequestDto);
        product.setId(id);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Product id=" + id + " wasn't found")))
                .doOnError(p -> log.warn("Product id=" + id + " wasn't found"))
                .flatMap(p -> productRepository.save(product))
                .map(productMapper::toDto)
                .doOnSuccess(p -> log.info("Product id={} have been updated", p.getId()));
    }

    /**
     * Find all products
     *
     * @return Flux<ProductResponseDto>
     */
    public Flux<ProductResponseDto> findAll() {
        log.info("Start to find all products");
        return productRepository.findAll()
                .map(productMapper::toDto)
                .doOnSubscribe(p -> log.info("All products have been found"));
    }

    /**
     * Find product by id
     *
     * @param id product id
     * @return ProductResponseDto
     * @throws ModelNotFoundException Product with id wasn't found
     */
    public Mono<ProductResponseDto> findById(Long id) {
        log.info("Start to find product by id={}", id);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Product id=" + id + " wasn't found")))
                .doOnError(p -> log.warn("Product id=" + id + " wasn't found"))
                .map(productMapper::toDto)
                .doOnSuccess(p -> log.info("Product: {} have been found", p));
    }

    /**
     * Find all products by containing name
     *
     * @param name product containing name
     * @return ProductResponseDto
     * @throws ModelNotFoundException Products containing name wasn't found
     */
    public Flux<ProductResponseDto> findByNameContaining(String name) {
        log.info("Start to find all products containing name={}", name);
        return productRepository.findByNameContaining(name)
                .switchIfEmpty(Flux.error(new ModelNotFoundException("Products containing name=" + name + " wasn't found")))
                .doOnError(p -> log.warn("Products containing name={} wasn't found", name))
                .map(productMapper::toDto)
                .doOnSubscribe(p -> log.info("All products containing name={} have been found", name));
    }

    /**
     * Delete product by id
     *
     * @param id product id
     */
    @Transactional
    public Mono<Void> deleteById(Long id) {
        log.info("Start to delete product by id={}", id);
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Product id=" + id + " wasn't found")))
                .doOnError(p -> log.warn("Product id=" + id + " wasn't found"))
                .flatMap(productRepository::delete)
                .doOnSuccess(p -> log.info("Product id={} have been deleted", id));
    }

    /**
     * Delete all products
     */
    @Transactional
    public Mono<Void> deleteAll() {
        log.info("Start to delete all products");
        return productRepository.deleteAll()
                .doOnSuccess(p -> log.info("All products have been deleted"));
    }

    /**
     * Check availability of Products by id list
     *
     * @param productIds id list
     * @return True if all ids exist in the repository
     */
    public Mono<Boolean> existsProductByIdIn(List<Long> productIds) {
        return productRepository.existsProductByIdIn(productIds, productIds.size());
    }

}

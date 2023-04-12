package com.estore.service;

import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.model.Product;
import com.estore.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * The class {@link ProductServiceTest} implements unit tests of service methods
 * that use {@link ProductRepository} to work with {@link Product} class objects.
 *
 * @author Dmytro Trotsenko on 4/9/23
 */

@SpringBootTest
@Slf4j
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductRepository productRepository;

    private final List<Product> productList = Arrays.asList(
            new Product(),
            new Product(1L, "product-1", "description-1", BigDecimal.valueOf(10.0)),
            new Product(2L, "product-2", "description-2", BigDecimal.valueOf(20.0)),
            new Product(3L, "product-3", "description-3", BigDecimal.valueOf(30.0))

    );

    @Test
    @DisplayName("Test find all products")
    public void testFindAll() {
        log.info("Starting testFindAll");

        when(productRepository.findAll()).thenReturn(Flux.fromIterable(productList));

        StepVerifier.create(productService.findAll())
                .expectNextCount(productList.size())
                .verifyComplete();

        log.info("Test testFindAll completed.");
    }

    @Test
    @DisplayName("Test find product by id")
    public void testFindById() {
        log.info("Starting testFindById");

        Product product = productList.get(1);

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.findById(1L))
                .expectNext(objectMapper.convertValue(product, ProductResponseDto.class))
                .verifyComplete();

        log.info("Test testFindById completed.");
    }

    @Test
    @DisplayName("Test find product by id when id does not exist")
    public void testFindByIdWhenIdNotExist() {
        log.info("Starting testFindByIdWhenIdNotExist");

        when(productRepository.findById(4L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.findById(4L))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testFindByIdWhenIdNotExist completed.");
    }

    @Test
    @DisplayName("Test create new product")
    public void testCreate() {
        log.info("Starting testCreate");

        Product newProduct = new Product(null, "newProduct", "description", BigDecimal.valueOf(40.55));

        when(productRepository.findByName(newProduct.getName())).thenReturn(Mono.empty());
        when(productRepository.save(newProduct)).thenReturn(Mono.just(newProduct));

        StepVerifier.create(productService.create(objectMapper.convertValue(newProduct, ProductRequestDto.class)))
                .expectNext(objectMapper.convertValue(newProduct, ProductResponseDto.class))
                .verifyComplete();

        log.info("Test testCreate completed.");
    }

    @Test
    @DisplayName("Test create new Product when ProductName already exist")
    public void testCreateWhenProductNameAlreadyExist() {
        log.info("Starting testCreateWhenProductNameAlreadyExist");

        Product newProduct = new Product(null, "newProduct", "description", BigDecimal.valueOf(40.55));

        when(productRepository.findByName(newProduct.getName())).thenReturn(Mono.just(newProduct));
        when(productRepository.save(newProduct)).thenReturn(Mono.just(newProduct));

        StepVerifier.create(productService.create(objectMapper.convertValue(newProduct, ProductRequestDto.class)))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testCreateWhenProductNameAlreadyExist completed.");
    }

    @Test
    @DisplayName("Test update product")
    public void testUpdate() {
        log.info("Starting testUpdate");

        Product product = productList.get(1);
        Product updatedProduct = new Product(1L, "updatedProduct", "description", BigDecimal.valueOf(10.55));

        when(productRepository.save(updatedProduct)).thenReturn(Mono.just(updatedProduct));
        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.update(1L, objectMapper.convertValue(updatedProduct, ProductRequestDto.class)))
                .expectNext(objectMapper.convertValue(updatedProduct, ProductResponseDto.class))
                .verifyComplete();

        log.info("Test testUpdate completed.");
    }

    @Test
    @DisplayName("Test update product when id does not exist")
    public void testUpdateWhenIdNotExist() {
        log.info("Starting testUpdateWhenIdNotExist");

        Product updatedProduct = new Product(1L, "updatedProduct", "description", BigDecimal.valueOf(10.55));

        when(productRepository.save(updatedProduct)).thenReturn(Mono.just(updatedProduct));
        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.update(1L, objectMapper.convertValue(updatedProduct, ProductRequestDto.class)))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testUpdateWhenIdNotExist completed.");
    }

    @Test
    @DisplayName("Test delete product by id")
    public void testDeleteById() {
        log.info("Starting testDeleteById");

        Product product = productList.get(1);

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));
        when(productRepository.delete(product)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteById(1L))
                .expectComplete()
                .verify();

        log.info("Test testDeleteById completed.");
    }

    @Test
    @DisplayName("Test delete product by id when id dos not exist")
    public void testDeleteByIdWhenIdNotExist() {
        log.info("Starting testDeleteByIdWhenIdNotExist");

        when(productRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteById(1L))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testDeleteByIdWhenIdNotExist completed.");
    }

    @Test
    @DisplayName("Test find by name containing")
    public void testFindByNameContaining() {
        log.info("Starting testFindByNameContaining");

        List<Product> filteredProducts = productList.stream()
                .skip(1)
                .filter(p -> p.getName().contains("prod"))
                .toList();
        when(productRepository.findByNameContaining("prod")).thenReturn(Flux.fromIterable(filteredProducts));

        List<ProductResponseDto> filteredProductsResponseDto = filteredProducts.stream()
                .map(p -> objectMapper.convertValue(p, ProductResponseDto.class))
                .toList();
        StepVerifier.create(productService.findByNameContaining("prod"))
                .expectNextSequence(filteredProductsResponseDto)
                .verifyComplete();

        log.info("Test testFindByNameContaining completed.");
    }

    @Test
    @DisplayName("Test find by name containing when name dos not exist")
    public void testFindByNameContainingWhenNameNotExist() {
        log.info("Starting testFindByNameContainingWhenNameNotExist");

        when(productRepository.findByNameContaining("name")).thenReturn(Flux.empty());

        StepVerifier.create(productService.findByNameContaining("name"))
                .expectError(EntityNotFoundException.class)
                .verify();

        log.info("Test testFindByNameContainingWhenNameNotExist completed.");
    }

}

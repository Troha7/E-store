package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.controller.api.ProductRestController;
import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class {@link ProductRestControllerTest} provides integration tests for the {@link ProductRestController} class,
 * testing its API endpoints.
 * <p>The tests are performed using a test container with a PostgreSQL database.</p>
 * <p>{@link TestContainerConfig} is the class for test container configuration.</p>
 *
 * @author Dmytro Trotsenko on 4/20/23
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainerConfig.class)
public class ProductRestControllerTest {

    @Autowired
    private ProductService productService;
    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int randomServerPort;

    private static final String URI = "/products";

    private final Long NOT_EXISTED_ID = 100L;

    private final List<ProductRequestDto> products = List.of(
            new ProductRequestDto("laptop", "Lenovo", BigDecimal.valueOf(3550.95)),
            new ProductRequestDto("phone", "Xiaomi", BigDecimal.valueOf(6700.55)),
            new ProductRequestDto("smartTV", "Samsung", BigDecimal.valueOf(9670.19))
    );

    @BeforeEach
    public void setup() {
        String localHost = "http://localhost:";
        webTestClient = WebTestClient.bindToServer()
                .baseUrl(localHost + randomServerPort)
                .build();
    }

    @AfterEach
    public void cleanup() {
        productService.deleteAll().subscribe();
    }

    //-----------------------------------
    //               GET
    //-----------------------------------

    @Test
    void shouldReturnEmptyListOfAllProducts() {

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> assertTrue(productList.isEmpty()));
    }

    @Test
    void shouldReturnAllProducts() {

        var savedProducts = saveToRepository(products);

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> {
                    assertEquals(3, productList.size());
                    assertIterableEquals(savedProducts, productList);
                });
    }

    @Test
    void shouldReturnAllProductsByContainingName() {

        var savedProducts = saveToRepository(products);
        String name = "TV";

        webTestClient.get().uri(URI.concat("?name={name}"), name)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> {
                    assertEquals(1, productList.size());
                    assertIterableEquals(savedProducts.subList(2, 3), productList);
                });
    }

    @Test
    void shouldThrowExceptionIfProductDoesNotContainsName() {

        saveToRepository(products);
        String name = "nameDoseNotExist";

        webTestClient.get().uri(URI.concat("?name={name}"), name)
                .exchange()
                .expectStatus().is5xxServerError();
    }


    @Test
    void shouldReturnProductById() {

        var savedProducts = saveToRepository(products);
        Long id = savedProducts.get(0).getId();

        webTestClient.get().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> {
                    assertEquals(1, productList.size());
                    assertIterableEquals(savedProducts.subList(0, 1), productList);
                });
    }

    @Test
    void shouldThrowExceptionIfProductIdDoesNotExist() {

        saveToRepository(products);

        webTestClient.get().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               POST
    //-----------------------------------

    @Test
    void shouldCreatedNewProduct() {
        var newProduct = new ProductRequestDto("newProduct", "new", BigDecimal.ZERO);
        var savedProduct = objectMapper.convertValue(newProduct, ProductResponseDto.class);

        webTestClient.post().uri(URI)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDto.class)
                .value(product -> assertEquals(savedProduct, product));

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionIfProductNameAlreadyExist() {

        saveToRepository(products);
        var existedProduct = products.get(0);

        webTestClient.post().uri(URI)
                .bodyValue(existedProduct)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               PUT
    //-----------------------------------

    @Test
    void shouldUpdatedExistingProduct() {

        List<ProductResponseDto> savedProducts = saveToRepository(products);
        var productForUpdate = new ProductRequestDto("updateProduct", "update", BigDecimal.ZERO);
        var updatedProduct = objectMapper.convertValue(productForUpdate, ProductResponseDto.class);
        Long id = savedProducts.get(0).getId();

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(productForUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDto.class)
                .value(product -> assertEquals(updatedProduct, product));

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(savedProducts.size())
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionIfUpdatedProductIdDoesNotExist() {

        saveToRepository(products);
        var productForUpdate = new ProductRequestDto("updateProduct", "update", BigDecimal.ZERO);

        webTestClient.put().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .bodyValue(productForUpdate)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               DELETE
    //-----------------------------------

    @Test
    void shouldDeleteAllProducts() {

        saveToRepository(products);

        webTestClient.delete().uri(URI)
                .exchange()
                .expectStatus().isNoContent()
                .expectBodyList(Void.class);

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void shouldDeleteProductById() {

        List<ProductResponseDto> savedProducts = saveToRepository(products);
        Long id = savedProducts.get(2).getId();

        webTestClient.delete().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody(Void.class);

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(savedProducts.size() - 1)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionIfDeletedProductIdDoesNotExist() {

        webTestClient.delete().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    @NotNull
    private List<ProductResponseDto> saveToRepository(List<ProductRequestDto> productList) {
        return productList.stream()
                .map(p -> productService.create(p).block())
                .toList();
    }

}

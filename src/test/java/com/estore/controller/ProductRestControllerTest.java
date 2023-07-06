package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.controller.rest.ProductRestController;
import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.model.Product;
import com.estore.repository.ProductRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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
    private ProductRepository productRepository;
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
    @WithMockUser
    void shouldReturnEmptyListOfAllProducts() {

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> assertTrue(productList.isEmpty()));
    }

    @Test
    @WithMockUser
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
    @WithMockUser
    void shouldReturnAllProductsByContainingName() {

        var savedProducts = saveToRepository(products);
        String name = "lap";

        webTestClient.get().uri(URI.concat("?name={name}"), name)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductResponseDto.class)
                .value(productList -> {
                    assertEquals(1, productList.size());
                    assertIterableEquals(savedProducts.subList(0, 1), productList);
                });
    }

    @Test
    @WithMockUser
    void shouldThrowExceptionIfProductDoesNotContainsName() {

        saveToRepository(products);
        String name = "nameDoseNotExist";

        webTestClient.get().uri(URI.concat("?name={name}"), name)
                .exchange()
                .expectStatus().isNotFound();
    }


    @Test
    @WithMockUser
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
    @WithMockUser
    void shouldThrowExceptionIfProductIdDoesNotExist() {

        saveToRepository(products);

        webTestClient.get().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               POST
    //-----------------------------------

    @Test
    @WithMockUser
    void shouldCreatedNewProduct() {
        var newProduct = new ProductRequestDto("newProduct", "new", BigDecimal.ONE);
        var savedProduct = objectMapper.convertValue(newProduct, ProductResponseDto.class);

        webTestClient.post().uri(URI)
                .bodyValue(newProduct)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponseDto.class)
                .value(product -> product.setId(null))
                .value(product -> assertEquals(savedProduct, product));

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @WithMockUser
    void shouldThrowExceptionIfProductNameAlreadyExist() {

        saveToRepository(products);
        var existedProduct = products.get(0);

        webTestClient.post().uri(URI)
                .bodyValue(existedProduct)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               PUT
    //-----------------------------------

    @Test
    @WithUserDetails("admin")
    void shouldUpdatedExistingProduct() {

        List<ProductResponseDto> savedProducts = saveToRepository(products);
        var productForUpdate = new ProductRequestDto("updateProduct", "update", BigDecimal.ONE);
        var updatedProduct = objectMapper.convertValue(productForUpdate, ProductResponseDto.class);
        Long id = savedProducts.get(0).getId();

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(productForUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductResponseDto.class)
                .value(product -> product.setId(null))
                .value(product -> assertEquals(updatedProduct, product));

        productService.findAll()
                .as(StepVerifier::create)
                .expectNextCount(savedProducts.size())
                .verifyComplete();
    }

    @Test
    @WithUserDetails("admin")
    void shouldThrowExceptionIfUpdatedProductIdDoesNotExist() {

        saveToRepository(products);
        var productForUpdate = new ProductRequestDto("updateProduct", "update", BigDecimal.ONE);

        webTestClient.put().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .bodyValue(productForUpdate)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               DELETE
    //-----------------------------------

    @Test
    @WithUserDetails("admin")
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
    @WithUserDetails("admin")
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
    @WithUserDetails("admin")
    void shouldThrowExceptionIfDeletedProductIdDoesNotExist() {

        webTestClient.delete().uri(URI.concat("/{id}"), NOT_EXISTED_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    @NotNull
    private List<ProductResponseDto> saveToRepository(List<ProductRequestDto> productList) {
        return Objects.requireNonNull(Flux.fromIterable(productList)
                .map(p -> new Product(null, p.getName(), p.getDescription(), p.getPrice()))
                .flatMap(p -> productRepository.findByName(p.getName())
                        .switchIfEmpty(Mono.defer(() -> productRepository.save(p))))
                .map(p -> objectMapper.convertValue(p, ProductResponseDto.class))
                .collectList().block());
    }

}

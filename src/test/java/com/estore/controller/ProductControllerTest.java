package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProductControllerTest}
 *
 * @author Dmytro Trotsenko on 4/20/23
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainerConfig.class)
@ActiveProfiles("application-test")
@Slf4j
public class ProductControllerTest {

    private static final String URI = "/products";

    private WebTestClient webTestClient;

    @Autowired
    private ProductService productService;
    @Autowired
    private ObjectMapper objectMapper;

    @LocalServerPort
    private int randomServerPort;

    private final List<ProductRequestDto> products = List.of(
            new ProductRequestDto("laptop", "Lenovo", BigDecimal.valueOf(3550.95)),
            new ProductRequestDto("phone", "Xiaomi", BigDecimal.valueOf(6700.55)),
            new ProductRequestDto("smartTV", "Samsung", BigDecimal.valueOf(9670.19))
    );

    @BeforeEach
    public void setup() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + randomServerPort).build();
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
                    assertProductListsEquals(savedProducts, productList);
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
                    assertProductListsEquals(savedProducts.subList(2, 3), productList);
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
                    assertProductListsEquals(savedProducts.subList(0, 1), productList);
                });
    }

    @Test
    void shouldThrowExceptionIfProductIdDoesNotExist() {

        saveToRepository(products);
        Long id = 100L;

        webTestClient.get().uri(URI.concat("/{id}"), id)
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
                .value(product -> assertProductEquals(savedProduct, product));
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

    private void assertProductListsEquals(List<ProductResponseDto> expectedProducts, List<ProductResponseDto> actualProducts) {
        IntStream.range(0, actualProducts.size())
                .forEach(i -> assertProductEquals(expectedProducts.get(i), actualProducts.get(i)));
    }

    private void assertProductEquals(ProductResponseDto expectedProduct, ProductResponseDto actualProduct) {
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());
        assertEquals(expectedProduct.getPrice(), actualProduct.getPrice());
    }

}

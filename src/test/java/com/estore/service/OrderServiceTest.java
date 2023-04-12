package com.estore.service;

import com.estore.dto.response.OrderResponseDto;
import com.estore.model.Order;
import com.estore.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * The class {@link OrderServiceTest} implements unit tests of service methods
 * that use {@link OrderRepository} to work with {@link com.estore.model.Order} class objects.
 *
 * @author Dmytro Trotsenko on 4/12/23
 */

@SpringBootTest
@Slf4j
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderRepository orderRepository;

    private final List<Order> orderList = Arrays.asList(
            new Order(),

            new Order(1L,null, LocalDate.now()),
            new Order(2L,null, LocalDate.now())
    );

    @Test
    @DisplayName("Test create new Order")
    public void testCreate() {
        log.info("Starting testCreate");

        Order newOrder = new Order(null, null, LocalDate.now());

        when(orderRepository.save(newOrder)).thenReturn(Mono.just(newOrder));

        StepVerifier.create(orderService.create())
                .expectNext(objectMapper.convertValue(newOrder, OrderResponseDto.class))
                .verifyComplete();

        log.info("Test testCreate completed.");
    }


}

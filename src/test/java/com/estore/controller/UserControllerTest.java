package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.service.OrderService;
import com.estore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class {@link UserControllerTest} provides integration tests for the {@link UserController} class,
 * testing its API endpoints.
 * <p>The tests are performed using a test container with a PostgreSQL database.</p>
 * <p>{@link TestContainerConfig} is the class for test container configuration.</p>
 *
 * @author Dmytro Trotsenko on 5/12/23
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainerConfig.class)
public class UserControllerTest {

    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    private WebTestClient webTestClient;

    @LocalServerPort
    private int randomServerPort;

    private static final String URI = "/users";

    private final Long NOT_EXISTED_USER_ID = 100L;

    private final List<UserRequestDto> users = List.of(
            new UserRequestDto("User1", "user1@gmail.com", "+380991111111", "1234"),
            new UserRequestDto("User2", "user2@gmail.com", "+380992222222", "4321"),
            new UserRequestDto("User3", "user3@gmail.com", "+380993333333", "2431")
    );

    private final List<AddressRequestDto> addresses = List.of(
            new AddressRequestDto("Kyiv", "str1", "1"),
            new AddressRequestDto("Kharkiv", "str2", "2"),
            new AddressRequestDto("London", "Baker Str", "221B")
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
        userService.deleteAll().subscribe();
    }

    //-----------------------------------
    //               GET
    //-----------------------------------

    @Test
    void shouldReturnEmptyListOfAllUsers() {

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseDto.class)
                .value(userList -> assertTrue(userList.isEmpty()));
    }

    @Test
    void shouldReturnAllUsersWithAddress() {
        int usersNum = 3;
        List<UserResponseDto> savedUsers = createUsersWithAddress(3);

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseDto.class)
                .value(userList -> {
                    assertEquals(usersNum, userList.size());
                    assertIterableEquals(savedUsers, userList);
                });
    }

    @Test
    void shouldReturnUserById() {

        var savedUser = createUsersWithAddress(3).get(0);
        Long id = savedUser.getId();

        webTestClient.get().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(savedUser, user));
    }

    @Test
    void shouldReturnUserWithOrdersHistoryById() {

        var savedUser = createUserWithOrder();
        assert savedUser != null;
        Long id = savedUser.getId();

        webTestClient.get().uri(URI.concat("/ordersHistory/{id}"), id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(savedUser, user));
    }

    @Test
    void shouldThrowExceptionIfUserIdDoesNotExist() {

        webTestClient.get().uri(URI.concat("/{id}"), NOT_EXISTED_USER_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               POST
    //-----------------------------------

    @Test
    void shouldCreatedNewOrder() {

        var expectedUser = new UserResponseDto(null, "User1", "user1@gmail.com", "+380991111111", "1234", null, null);

        webTestClient.post().uri(URI)
                .bodyValue(users.get(0))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    void shouldAddedAddressByUserId() {

        var expectedUser = userService.createUser(users.get(0)).block();
        assert expectedUser != null;
        expectedUser.setAddress(new AddressResponseDto(null, "London", "Baker Str", "221B"));
        Long userId = expectedUser.getId();

        webTestClient.post().uri(URI.concat("/{userId}"), userId)
                .bodyValue(addresses.get(2))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    void shouldUpdatedAddressByUserIdIfAddressExist() {

        var expectedUser = createUsersWithAddress(1).get(0);
        expectedUser.setAddress(new AddressResponseDto(null, "London", "Baker Str", "221B"));
        Long userId = expectedUser.getId();

        webTestClient.post().uri(URI.concat("/{userId}"), userId)
                .bodyValue(addresses.get(2))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    void shouldThrowExceptionIfAddedAddressByUserIdDoesNotExist() {

        createUsersWithAddress(1);

        webTestClient.post().uri(URI.concat("/{userId}"), NOT_EXISTED_USER_ID)
                .bodyValue(addresses.get(2))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               PUT
    //-----------------------------------

    @Test
    void shouldUpdatedExistingUserById() {

        var expectedUser = userService.createUser(users.get(0)).block();
        assert expectedUser != null;
        Long id = expectedUser.getId();

        var updatedUser = users.get(2);

        expectedUser.setName(updatedUser.getName());
        expectedUser.setEmail(updatedUser.getEmail());
        expectedUser.setPhone(updatedUser.getPhone());
        expectedUser.setPassword(updatedUser.getPassword());

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    void shouldThrowExceptionIfUpdatedUserByIdDoesNotExist() {

        userService.createUser(users.get(0)).block();

        var updatedUser = users.get(2);

        webTestClient.put().uri(URI.concat("/{id}"), NOT_EXISTED_USER_ID)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    //-----------------------------------
    //               DELETE
    //-----------------------------------

    @Test
    void shouldDeleteUserByIdAndAllUserRelations() {

        var expectedUser = createUsersWithAddress(1).get(0);
        Long id = expectedUser.getId();

        webTestClient.delete().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isNoContent();

        userService.findById(id)
                .as(StepVerifier::create)
                .expectError(EntityNotFoundException.class)
                .verify();

        userService.findAddressByUserId(id)
                .as(StepVerifier::create)
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    @Nullable
    private UserResponseDto createUserWithOrder() {
        return userService.createUser(users.get(0))
                .flatMap(user -> orderService.create(user.getId())
                        .flatMap(order -> orderService.findAll()
                                .collectList()
                                .map(orders -> {
                                    user.setOrdersHistory(orders);
                                    return user;
                                }))).block();
    }

    private @NotNull List<UserResponseDto> createUsersWithAddress(int num) {
        return IntStream.range(0, num)
                .mapToObj(i -> userService.createUser(users.get(i))
                        .flatMap(user -> userService.addAddress(user.getId(), addresses.get(i)))
                        .block())
                .toList();
    }

}

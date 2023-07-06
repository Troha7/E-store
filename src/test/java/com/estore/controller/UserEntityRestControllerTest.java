package com.estore.controller;

import com.estore.configuration.TestContainerConfig;
import com.estore.controller.rest.UserRestController;
import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.exception.ModelNotFoundException;
import com.estore.service.OrderService;
import com.estore.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.IntStream;

import static com.estore.model.UserRole.USER;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class {@link UserEntityRestControllerTest} provides integration tests for the {@link UserRestController} class,
 * testing its API endpoints.
 * <p>The tests are performed using a test container with a PostgreSQL database.</p>
 * <p>{@link TestContainerConfig} is the class for test container configuration.</p>
 *
 * @author Dmytro Trotsenko on 5/12/23
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(TestContainerConfig.class)
public class UserEntityRestControllerTest {

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
            new UserRequestDto("User1", "1234", USER, "First1", "Last1", "user1@gmail.com", "+380991111111"),
            new UserRequestDto("User2", "1594", USER, "First2", "Last2", "user2@gmail.com", "+380992222222"),
            new UserRequestDto("User3", "0031", USER, "First3", "Last3", "user3@gmail.com", "+380993333333")
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
    @WithMockUser
    void shouldReturnEmptyListOfAllUsers() {

        webTestClient.get().uri(URI)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserResponseDto.class)
                .value(userList -> assertTrue(userList.isEmpty()));
    }

    @Test
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
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
    @WithMockUser
    void shouldThrowExceptionIfUserIdDoesNotExist() {

        webTestClient.get().uri(URI.concat("/{id}"), NOT_EXISTED_USER_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               POST
    //-----------------------------------

    @Test
    @WithMockUser
    void shouldCreatedNewUser() {

        var newUser = users.get(0);

        var expectedUser = new UserResponseDto();
        expectedUser.setUsername(newUser.getUsername());
        expectedUser.setRole(USER);
        expectedUser.setFirstName(newUser.getFirstName());
        expectedUser.setLastName(newUser.getLastName());
        expectedUser.setEmail(newUser.getEmail());
        expectedUser.setPhone(newUser.getPhone());

        webTestClient.post().uri(URI)
                .bodyValue(newUser)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .value(user -> {
                    user.setId(null);
                    user.setPassword(null);
                })
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    @WithMockUser
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
                .value(user -> user.getAddress().setId(null))
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    @WithMockUser
    void shouldUpdatedAddressByUserIdIfAddressExist() {

        var expectedUser = createUsersWithAddress(1).get(0);
        expectedUser.setAddress(new AddressResponseDto(null, "London", "Baker Str", "221B"));
        Long userId = expectedUser.getId();

        webTestClient.post().uri(URI.concat("/{userId}"), userId)
                .bodyValue(addresses.get(2))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponseDto.class)
                .value(user -> user.getAddress().setId(null))
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    @WithMockUser
    void shouldThrowExceptionIfAddedAddressByUserIdDoesNotExist() {

        createUsersWithAddress(1);

        webTestClient.post().uri(URI.concat("/{userId}"), NOT_EXISTED_USER_ID)
                .bodyValue(addresses.get(2))
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               PUT
    //-----------------------------------

    @Test
    @WithMockUser
    void shouldUpdatedExistingUserById() {

        var expectedUser = userService.createUser(users.get(0)).block();
        assert expectedUser != null;
        Long id = expectedUser.getId();

        var updatedUser = users.get(2);

        expectedUser.setUsername(updatedUser.getUsername());
        expectedUser.setPassword(updatedUser.getPassword());
        expectedUser.setFirstName(updatedUser.getFirstName());
        expectedUser.setLastName(updatedUser.getLastName());
        expectedUser.setEmail(updatedUser.getEmail());
        expectedUser.setPhone(updatedUser.getPhone());

        webTestClient.put().uri(URI.concat("/{id}"), id)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponseDto.class)
                .value(user -> assertEquals(expectedUser, user));
    }

    @Test
    @WithMockUser
    void shouldThrowExceptionIfUpdatedUserByIdDoesNotExist() {

        userService.createUser(users.get(0)).block();

        var updatedUser = users.get(2);

        webTestClient.put().uri(URI.concat("/{id}"), NOT_EXISTED_USER_ID)
                .bodyValue(updatedUser)
                .exchange()
                .expectStatus().isNotFound();
    }

    //-----------------------------------
    //               DELETE
    //-----------------------------------

    @Test
    @WithMockUser
    void shouldDeleteUserByIdAndAllUserRelations() {

        var expectedUser = createUsersWithAddress(1).get(0);
        Long id = expectedUser.getId();

        webTestClient.delete().uri(URI.concat("/{id}"), id)
                .exchange()
                .expectStatus().isNoContent();

        userService.findById(id)
                .as(StepVerifier::create)
                .expectError(ModelNotFoundException.class)
                .verify();

        userService.findAddressByUserId(id)
                .as(StepVerifier::create)
                .expectError(ModelNotFoundException.class)
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

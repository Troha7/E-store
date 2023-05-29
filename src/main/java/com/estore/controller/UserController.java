package com.estore.controller;

import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link UserController}
 *
 * @author Dmytro Trotsenko on 5/8/23
 */

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new User")
    public Mono<UserResponseDto> createUser(@Validated @RequestBody UserRequestDto user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update User")
    public Mono<UserResponseDto> updateUser(@PathVariable("id") long id, @Validated @RequestBody UserRequestDto user) {
        return userService.update(id, user);
    }

    @PostMapping("/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add user address")
    public Mono<UserResponseDto> addAddress(@PathVariable("userId") long userId, @Validated @RequestBody AddressRequestDto addressRequestDto) {
        return userService.addAddress(userId, addressRequestDto);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find User by id")
    public Mono<UserResponseDto> findById(@PathVariable("id") long id) {
        return userService.findById(id);
    }

    @GetMapping("/ordersHistory/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find User with OrdersHistory by userId")
    public Mono<UserResponseDto> findUserOrdersHistoryById(@PathVariable("id") long id) {
        return userService.findUserOrdersHistoryById(id);
    }

    @GetMapping("/fullInfo/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find Full User Info by userId")
    public Mono<UserResponseDto> findFullUserInfoById(@PathVariable("id") long id) {
        return userService.findFullUserInfoById(id);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find all Users")
    public Flux<UserResponseDto> findAll() {
        return userService.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete User by id")
    public Mono<Void> delete(@PathVariable("id") long id) {
        return userService.deleteById(id);
    }

}

package com.estore.service;

import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.model.Address;
import com.estore.model.User;
import com.estore.repository.AddressRepository;
import com.estore.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link UserService}
 *
 * @author Dmytro Trotsenko on 5/8/23
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    /**
     * Create a new User
     *
     * @return the saved user without the related entities
     */
    @Transactional
    public Mono<UserResponseDto> createUser(UserRequestDto userRequestDto) {
        log.info("Start to create User");
        User newUser = objectMapper.convertValue(userRequestDto, User.class);
        return userRepository.save(newUser)
                .map(user -> objectMapper.convertValue(user, UserResponseDto.class))
                .doOnSuccess(user -> log.info("User id={} have been created", user.getId()));
    }

    /**
     * Add Address to User by user id
     *
     * @param userId            user id
     * @param addressRequestDto address to be saved
     * @return the saved user with related address
     */
    @Transactional
    public Mono<UserResponseDto> addAddress(Long userId, AddressRequestDto addressRequestDto) {
        log.info("Start to addAddress by userId={}", userId);
        return getUserById(userId)
                .map(user -> objectMapper.convertValue(user, UserResponseDto.class))
                .flatMap(user -> updateAddress(userId, addressRequestDto)
                        .map(updatedAddress -> {
                            user.setAddress(updatedAddress);
                            return user;
                        }))
                .doOnSuccess(user -> log.info("Address has been added to User id={}", userId));
    }

    /**
     * Find User by id
     *
     * @param id user id
     * @return Find user with the related address loaded
     * @throws EntityNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findById(Long id) {
        log.info("Start to find user by id={}", id);
        return getUserById(id)
                .flatMap(this::loadAddress)
                .doOnSuccess(user -> log.info("User id={} have been found", user.getId()));
    }

    /**
     * Find User info and Orders history by user id
     *
     * @param id user id
     * @return Find user with the related orders history
     * @throws EntityNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findUserOrdersHistoryById(Long id) {
        log.info("Start to find User with OrdersHistory By userId={}", id);
        return getUserById(id)
                .map(user -> objectMapper.convertValue(user, UserResponseDto.class))
                .flatMap(this::loadOrdersHistory)
                .doOnSuccess(user -> log.info("User id={} with OrdersHistory have been found", user.getId()));
    }

    /**
     * Find full User info, address and Orders history, by user id
     *
     * @param id user id
     * @return Find user with the related address and orders history
     * @throws EntityNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findFullUserInfoById(Long id) {
        log.info("Start to find full User info by id={}", id);
        return getUserById(id)
                .flatMap(user -> loadAddress(user)
                        .flatMap(this::loadOrdersHistory))
                .doOnSuccess(user -> log.info("Full User info by id={} have been found", user.getId()));
    }

    /**
     * Find all Users
     *
     * @return Find all users with the related address loaded
     */
    public Flux<UserResponseDto> findAll() {
        log.info("Start to find all users");
        return userRepository.findAll()
                .flatMap(this::loadAddress)
                .doOnSubscribe(o -> log.info("All orders have been found"));
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    private Mono<UserResponseDto> loadOrdersHistory(UserResponseDto userDto) {
        return orderService.findAll()
                .collectList()
                .map(orders -> {
                    userDto.setOrdersHistory(orders);
                    return userDto;
                });
    }

    private Mono<UserResponseDto> loadAddress(User user) {
        return findByUserId(user.getId())
                .map(addressDto -> {
                    var userDto = objectMapper.convertValue(user, UserResponseDto.class);
                    userDto.setAddress(addressDto);
                    return userDto;
                });
    }

    private Mono<AddressResponseDto> findByUserId(Long id) {
        return addressRepository.findByUserId(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Address with userId=" + id + " wasn't found")))
                .map(address -> objectMapper.convertValue(address, AddressResponseDto.class))
                .doOnError(user -> log.warn("Address with userId=" + id + " wasn't found"));
    }

    private Mono<AddressResponseDto> updateAddress(Long userId, AddressRequestDto addressRequestDto) {

        Address newAddress = objectMapper.convertValue(addressRequestDto, Address.class);
        return addressRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> addressRepository.save(newAddress)
                        .doOnSuccess(address -> log.info("New Address id={} was created", address.getId()))))
                .map(address -> {
                    newAddress.setId(address.getId());
                    newAddress.setUserId(userId);
                    return address;
                })
                .flatMap(address -> addressRepository.save(newAddress))
                .map(address -> objectMapper.convertValue(address, AddressResponseDto.class))
                .doOnSuccess(address -> log.info("Address id={} was updated", address.getId()));
    }

    private Mono<User> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User id=" + id + " wasn't found")))
                .doOnError(user -> log.warn("User id=" + id + " wasn't found"));
    }

}

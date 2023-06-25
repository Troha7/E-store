package com.estore.service;

import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.exception.ModelNotFoundException;
import com.estore.mapper.AddressMapper;
import com.estore.mapper.UserMapper;
import com.estore.model.Address;
import com.estore.model.UserEntity;
import com.estore.repository.AddressRepository;
import com.estore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new User
     *
     * @return the saved user without the related entities
     */
    @Transactional
    public Mono<UserResponseDto> createUser(UserRequestDto userRequestDto) {
        log.info("Start to create User");
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        return userRepository.save(userMapper.toModel(userRequestDto))
                .map(userMapper::toUser)
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
                .map(userMapper::toUser)
                .flatMap(user -> saveAddress(userId, addressRequestDto)
                        .doOnNext(user::setAddress)
                        .map(savedAddress -> user))
                .doOnSuccess(user -> log.info("Address has been added to User id={}", userId));
    }

    /**
     * Updates an existing user.
     *
     * @param id             User id.
     * @param userRequestDto the updated user info.
     * @return Updated user.
     * @throws ModelNotFoundException User with id wasn't found.
     */
    @Transactional
    public Mono<UserResponseDto> update(Long id, UserRequestDto userRequestDto) {
        log.info("Start to update User");
        UserEntity updatedUser = userMapper.toModel(userRequestDto);
        return getUserById(id)
                .doOnNext(user -> updatedUser.setId(user.getId()))
                .flatMap(user -> userRepository.save(updatedUser))
                .map(userMapper::toUser)
                .doOnSuccess(user -> log.info("User id={} have been updated", user.getId()));
    }

    /**
     * Find User by id
     *
     * @param id user id
     * @return Find user with the related address loaded
     * @throws ModelNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findById(Long id) {
        log.info("Start to find user by id={}", id);
        return getUserById(id)
                .flatMap(this::loadAddress)
                .doOnSuccess(user -> log.info("User id={} have been found", user.getId()));
    }

    /**
     * Find User by Username
     *
     * @param username User name
     * @return Find user
     * @throws ModelNotFoundException Username wasn't found
     */
    public Mono<UserResponseDto> findByUsername(String username) {
        log.info("Start to find user by username={}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Username=" + username + " wasn't found")))
                .doOnError(user -> log.warn("Username=" + username + " wasn't found"))
                .map(userMapper::toUser)
                .doOnSuccess(user -> log.info("User id={} have been found", user.getId()));
    }

    /**
     * Find User info and Orders history by user id
     *
     * @param id user id
     * @return Find user with the related orders history
     * @throws ModelNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findUserOrdersHistoryById(Long id) {
        log.info("Start to find User with OrdersHistory By userId={}", id);
        return getUserById(id)
                .map(userMapper::toUser)
                .flatMap(this::loadOrdersHistory)
                .doOnSuccess(user -> log.info("User id={} with OrdersHistory have been found", user.getId()));
    }

    /**
     * Find full User info, address and Orders history, by user id
     *
     * @param id user id
     * @return Find user with the related address and orders history
     * @throws ModelNotFoundException User with id wasn't found
     */
    public Mono<UserResponseDto> findFullUserInfoById(Long id) {
        log.info("Start to find full User info by id={}", id);
        return getUserById(id)
                .flatMap(userEntity -> loadAddress(userEntity)
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

    /**
     * Deletes User by id.
     * Also deletes all related address and orders.
     *
     * @param id User id.
     * @return Mono<Void>
     * @throws ModelNotFoundException if the user is not found.
     */
    @Transactional
    public Mono<Void> deleteById(Long id) {
        log.info("Start to delete user by id={}", id);
        return getUserById(id)
                .flatMap(userEntity -> userRepository.deleteById(userEntity.getId()))
                .doOnSuccess(o -> log.info("User id={} has been deleted", id));
    }

    /**
     * Deletes all Users.
     * Also deletes all related address and orders.
     *
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteAll() {
        log.info("Start to delete all Users");
        return userRepository.deleteAll()
                .doOnSuccess(o -> log.info("All Users has been deleted"));
    }

    /**
     * Find Address by user id
     *
     * @param id user id
     * @return User address
     * @throws ModelNotFoundException Address with id wasn't found
     */
    public Mono<AddressResponseDto> findAddressByUserId(Long id) {
        log.info("Start to find Address by userId");
        return addressRepository.findByUserId(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("Address with userId=" + id + " wasn't found")))
                .map(addressMapper::toDto)
                .doOnError(user -> log.warn("Address with userId=" + id + " wasn't found"))
                .doOnSuccess(o -> log.info("Address by userId has been found"));
    }

    //-----------------------------------
    //         Private methods
    //-----------------------------------

    private Mono<UserResponseDto> loadOrdersHistory(UserResponseDto userDto) {
        return orderService.findAll()
                .collectList()
                .doOnNext(userDto::setOrdersHistory)
                .map(orders -> userDto);
    }

    private Mono<UserResponseDto> loadAddress(UserEntity user) {
        var userDto = userMapper.toUser(user);
        return findAddressByUserId(user.getId())
                .onErrorReturn(new AddressResponseDto())
                .doOnNext(userDto::setAddress)
                .map(addressDto -> userDto);
    }

    private Mono<AddressResponseDto> saveAddress(Long userId, AddressRequestDto addressRequestDto) {

        Address newAddress = addressMapper.toModel(addressRequestDto);
        newAddress.setUserId(userId);
        return addressRepository.findByUserId(userId)
                .switchIfEmpty(Mono.just(newAddress))
                .doOnNext(address -> {
                    if (address.getId() != null) {
                        newAddress.setId(address.getId());
                        log.info("Address {} was updated", address);
                    } else {
                        log.info("New Address {} was created", address);
                    }
                })
                .flatMap(address -> addressRepository.save(newAddress))
                .map(addressMapper::toDto);
    }

    private Mono<UserEntity> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ModelNotFoundException("User id=" + id + " wasn't found")))
                .doOnError(user -> log.warn("User id=" + id + " wasn't found"));
    }

}

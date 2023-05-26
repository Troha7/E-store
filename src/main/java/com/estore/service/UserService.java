package com.estore.service;

import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.model.Address;
import com.estore.model.UserEntity;
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
        UserEntity newUserEntity = objectMapper.convertValue(userRequestDto, UserEntity.class);
        return userRepository.save(newUserEntity)
                .map(userEntity -> objectMapper.convertValue(userEntity, UserResponseDto.class))
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
                .map(userEntity -> objectMapper.convertValue(userEntity, UserResponseDto.class))
                .flatMap(user -> saveAddress(userId, addressRequestDto)
                        .map(savedAddress -> {
                            user.setAddress(savedAddress);
                            return user;
                        }))
                .doOnSuccess(user -> log.info("Address has been added to User id={}", userId));
    }

    /**
     * Updates an existing user.
     *
     * @param id              User id.
     * @param userRequestDto  the updated user info.
     * @return Updated user.
     * @throws EntityNotFoundException User with id wasn't found.
     */
    @Transactional
    public Mono<UserResponseDto> update(Long id, UserRequestDto userRequestDto) {
        log.info("Start to update User");
        return getUserById(id)
                .map(userEntity -> {
                    UserEntity updatedUserEntity = objectMapper.convertValue(userRequestDto, UserEntity.class);
                    updatedUserEntity.setId(userEntity.getId());
                    return updatedUserEntity;
                })
                .flatMap(userRepository::save)
                .map(updatedUserEntity -> objectMapper.convertValue(updatedUserEntity, UserResponseDto.class))
                .doOnSuccess(user -> log.info("User id={} have been updated", user.getId()));
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
                .map(userEntity -> objectMapper.convertValue(userEntity, UserResponseDto.class))
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
     * @throws EntityNotFoundException if the user is not found.
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
     * @throws EntityNotFoundException Address with id wasn't found
     */
    public Mono<AddressResponseDto> findAddressByUserId(Long id) {
        log.info("Start to find Address by userId");
        return addressRepository.findByUserId(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Address with userId=" + id + " wasn't found")))
                .map(address -> objectMapper.convertValue(address, AddressResponseDto.class))
                .doOnError(user -> log.warn("Address with userId=" + id + " wasn't found"))
                .doOnSuccess(o -> log.info("Address by userId has been found"));
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

    private Mono<UserResponseDto> loadAddress(UserEntity userEntity) {
        return findAddressByUserId(userEntity.getId())
                .map(addressDto -> {
                    var userDto = objectMapper.convertValue(userEntity, UserResponseDto.class);
                    userDto.setAddress(addressDto);
                    return userDto;
                });
    }

    private Mono<AddressResponseDto> saveAddress(Long userId, AddressRequestDto addressRequestDto) {

        Address newAddress = objectMapper.convertValue(addressRequestDto, Address.class);
        newAddress.setUserId(userId);
        return addressRepository.findByUserId(userId)
                .switchIfEmpty(Mono.just(newAddress))
                .map(address -> {
                    if(address.getId() != null) {
                        newAddress.setId(address.getId());
                        log.info("Address {} was updated", address);
                    }else {
                        log.info("New Address {} was created", address);
                    }
                    return address;
                })
                .flatMap(address -> addressRepository.save(newAddress))
                .map(address -> objectMapper.convertValue(address, AddressResponseDto.class));
    }

    private Mono<UserEntity> getUserById(Long id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("User id=" + id + " wasn't found")))
                .doOnError(user -> log.warn("User id=" + id + " wasn't found"));
    }

}

package com.estore.repository;

import com.estore.model.Address;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * {@link AddressRepository}
 *
 * @author Dmytro Trotsenko on 4/13/23
 */

@Repository
public interface AddressRepository extends ReactiveCrudRepository<Address, Long> {

    Mono<Address> findByUserId(Long userId);
}

package com.estore.repository;

import com.estore.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

/**
 * {@link UserRepository}
 *
 * @author Dmytro Trotsenko on 4/13/23
 */

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
}

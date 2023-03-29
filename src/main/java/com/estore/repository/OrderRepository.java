package com.estore.repository;

import com.estore.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;


/**
 * {@link OrderRepository}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}

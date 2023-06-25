package com.estore.repository;

import com.estore.model.Order;
import com.estore.model.OrderStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;


/**
 * {@link OrderRepository}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

    Flux<Order> findAllOrderByUserId(Long id);

        @Query("""
                SELECT *
                FROM e_store.order
                WHERE status = :status 
                AND fk_user_id = (SELECT id FROM e_store.user WHERE username = :username)
                """)
    Flux<Order> findAllOrderByUsernameAndStatus(String username, OrderStatus status);
}

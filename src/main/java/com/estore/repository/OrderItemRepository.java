package com.estore.repository;

import com.estore.model.OrderItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * {@link OrderItemRepository}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

    Flux<OrderItem> findAllByOrderId(Long id);

    @Query("""
            SELECT COUNT(*) > 0
            FROM e_store.product p, e_store.order o
            WHERE  o.id=:order_id AND p.id=:product_id;
            """)
    Mono<Boolean> existByOrderIdAndProductId(Long orderId, Long productId);

}

package com.estore.repository;

import com.estore.model.OrderItem;
import com.estore.model.Product;
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

    //FIXME unused method
    Mono<Void> deleteAllByOrderId(Long id);


    //FIXME return products in OrderItemRepo
    @Query("SELECT * " +
            "FROM e_store.product p " +
            "JOIN e_store.order_item oi ON p.id = oi.fk_product_id " +
            "WHERE oi.fk_order_id = :order_id;")
    Flux<Product> findProductsByOrderId(Long orderId);

    @Query("SELECT o.id > 0 AND p.id > 0 " +
            "FROM e_store.product p, e_store.order o " +
            "WHERE  o.id=:order_id AND p.id=:prod_id;")
// FIXME @Query("""
//            SELECT COUNT(*) > 0
//            FROM order_item
//            WHERE fk_order_id = :orderId AND fk_product_id = :productId""")
    Mono<Boolean> existByOrderIdAndProductId(Long orderId, Long productId);

}

package com.estore.repository;

import com.estore.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link ProductRepository}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Mono<Product> findByName(String name);

    Flux<Product> findByNameContaining(String name);

    @Query("select p.*, oi.quantity " +
            "from e_store.product p " +
            "join e_store.order_item oi " +
            "on p.id = oi.fk_product_id " +
            "where oi.fk_order_id = :item_id " +
            "order by p.name")
    Flux<Product> findProductsByOrderId(Long orderId);

}

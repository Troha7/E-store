package com.estore.repository;

import com.estore.model.Product;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * {@link ProductRepository}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Repository
public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {

    Mono<Product> findByName(String name);

    Flux<Product> findByNameContaining(String name);

    @Query("""
            SELECT *
            FROM e_store.product p
            JOIN e_store.order_item oi ON p.id = oi.fk_product_id
            WHERE oi.fk_order_id = :order_id;
            """)
    Flux<Product> findProductsByOrderId(Long orderId);

    @Query("""
            SELECT COUNT(*) = :list_size
            FROM e_store.product p
            WHERE p.id IN (:list);
            """)
    Mono<Boolean> existsProductByIdIn(@Param("list") List<Long> productIds, @Param("list_size") int listSize);

}

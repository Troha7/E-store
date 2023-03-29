package com.estore.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * {@link OrderItem}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("e_store.order_item")
public class OrderItem {

    @Id
    private Long id;

    @Column("fk_order_id")
    private Long orderId;

    @Column("fk_product_id")
    private Long productId;

    @Column
    private Integer quantity;

}

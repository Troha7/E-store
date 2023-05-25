package com.estore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

/**
 * {@link Order}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private Long id;

    @Column("fk_user_id")
    private Long userId;

    @Column("order_date")
    private LocalDate date;

}

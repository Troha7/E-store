package com.estore.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * {@link Address}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("e_store.address")
public class Address {

    @Id
    private Long id;

    @Column
    private String city;

    @Column
    private String street;

    @Column
    private String house;

}

package com.estore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * {@link ProductRequestDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    private String name;

    private String description;

    private BigDecimal price;

}

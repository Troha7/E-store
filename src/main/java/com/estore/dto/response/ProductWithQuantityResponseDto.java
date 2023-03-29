package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * {@link ProductWithQuantityResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithQuantityResponseDto {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private int quantity;

}

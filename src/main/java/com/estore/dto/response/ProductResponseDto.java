package com.estore.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * {@link ProductResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

}

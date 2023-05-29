package com.estore.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link OrderItemRequestDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {

    @Min(value = 1, message = "{field.err.min}")
    private Long productId;

    @Min(value = 1, message = "{field.err.min}")
    @Max(value = 1000000, message = "{field.err.max}")
    private Integer quantity;

}

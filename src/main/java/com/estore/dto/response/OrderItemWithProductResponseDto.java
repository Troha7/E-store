package com.estore.dto.response;

import com.estore.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link OrderItemWithProductResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemWithProductResponseDto {

    private Long id;

    private Product product;

    private Integer quantity;

}

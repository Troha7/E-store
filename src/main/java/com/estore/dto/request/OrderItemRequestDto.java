package com.estore.dto.request;

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

    //private Long orderId;

    private Long productId;

    private Integer quantity;

}

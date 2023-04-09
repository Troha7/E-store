package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link OrderWithProductsResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderWithProductsResponseDto {

    private Long id;

    private LocalDate date;

    private List<OrderItemWithProductResponseDto> orderItems;

}
package com.estore.dto.response;

import com.estore.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * {@link OrderResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Long id;

    private Long userId;

    private LocalDate date;

    private List<OrderItemResponseDto> orderItems;

    private OrderStatus status;

    private BigDecimal totalPrice;

}

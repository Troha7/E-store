package com.estore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * {@link OrderRequestDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    private LocalDate date;

    private List<OrderItemRequestDto> Products;

}

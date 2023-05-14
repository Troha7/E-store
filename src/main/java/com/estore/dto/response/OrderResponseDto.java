package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

    private LocalDate date;

    private List<OrderItemResponseDto> orderItems;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderResponseDto that = (OrderResponseDto) o;
        return date.equals(that.date) && Objects.equals(orderItems, that.orderItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, orderItems);
    }
}

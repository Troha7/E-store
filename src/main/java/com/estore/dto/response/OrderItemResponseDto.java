package com.estore.dto.response;

import com.estore.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * {@link OrderItemResponseDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {

    private Long id;

    private Product product;

    private Integer quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemResponseDto that = (OrderItemResponseDto) o;
        return Objects.equals(product, that.product) && quantity.equals(that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, quantity);
    }
}

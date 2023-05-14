package com.estore.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductResponseDto that = (ProductResponseDto) o;
        return name.equals(that.name) && description.equals(that.description) && price.equals(that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, price);
    }
}

package com.estore.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * {@link ProductRequestDto}
 *
 * @author Dmytro Trotsenko on 3/9/23
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 25, message = "{field.err.size}")
    private String name;

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 64, message = "{field.err.size}")
    private String description;

    @NotNull(message = "{field.err.null}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{field.err.decimal.min}")
    @Digits(integer = 6, fraction = 2, message = "{field.err.decimal.digits}")
    private BigDecimal price;

}

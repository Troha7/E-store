package com.estore.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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

    @NotNull(message = "{field.err.null}")
    @PastOrPresent(message = "{field.err.date}")
    private LocalDate date;

    @Valid
    private List<OrderItemRequestDto> products;

}

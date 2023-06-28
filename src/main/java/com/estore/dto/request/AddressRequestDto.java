package com.estore.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link AddressRequestDto}
 *
 * @author Dmytro Trotsenko on 5/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequestDto {

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 15, message = "{field.err.size}")
    private String city;

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 15, message = "{field.err.size}")
    private String street;

    @NotNull(message = "{field.err.null}")
    @Size(min = 1, max = 5, message = "{field.err.size}")
    private String house;

}

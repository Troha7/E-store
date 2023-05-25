package com.estore.dto.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link AddressRequestDto}
 *
 * @author Dmytro Trotsenko on 5/9/23
 */

//FIXME do not use @Data
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequestDto {

    //Annotations for @Valid, also hibernate-validator
    @NotEmpty
    @Size(min = 4)
    private String city;

    private String street;

    private String house;

}

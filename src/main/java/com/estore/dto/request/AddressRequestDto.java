package com.estore.dto.request;


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

    private String city;

    private String street;

    private String house;

}

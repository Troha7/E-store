package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * {@link AddressResponseDto}
 *
 * @author Dmytro Trotsenko on 5/9/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressResponseDto {

    private Long id;

    private String city;

    private String street;

    private String house;

}

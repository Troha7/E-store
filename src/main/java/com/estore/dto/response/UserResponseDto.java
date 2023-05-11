package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * {@link UserResponseDto}
 *
 * @author Dmytro Trotsenko on 5/8/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private String password;

    private AddressResponseDto address;

    private List<OrderResponseDto> ordersHistory;

}

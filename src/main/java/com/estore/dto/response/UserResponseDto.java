package com.estore.dto.response;

import com.estore.model.UserRole;
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

    private String username;

    private String password;

    private UserRole role;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private AddressResponseDto address;

    private List<OrderResponseDto> ordersHistory;

}

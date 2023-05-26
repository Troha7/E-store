package com.estore.dto.request;

import com.estore.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * {@link UserRequestDto}
 *
 * @author Dmytro Trotsenko on 5/8/23
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    private String username;

    private String password;

    private UserRole role;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

}

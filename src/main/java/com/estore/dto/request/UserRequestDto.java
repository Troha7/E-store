package com.estore.dto.request;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 25, message = "{field.err.size}")
    private String name;
    private String username;

    private String password;

    private UserRole role;

    private String firstName;

    private String lastName;

    @Pattern(regexp = "([a-z]+[0-9])+@([a-z])+\\.com", message = "{field.err.email}")
    @Size(min = 10, max = 45, message = "{field.err.size}")
    private String email;

    @NotNull(message = "{field.err.null}")
    @Pattern(regexp = "[+]\\d{12}$", message = "{field.err.phone}")
    private String phone;

    @NotNull(message = "{field.err.null}")
    @Size(min = 4, max = 64, message = "{field.err.size}")
    private String password;

}

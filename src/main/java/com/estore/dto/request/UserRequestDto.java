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
    @Size(min = 3, max = 64, message = "{field.err.size}")
    private String username;

    @NotNull(message = "{field.err.null}")
    @Size(min = 4, max = 2048, message = "{field.err.size}")
    private String password;

    private UserRole role;
    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 64, message = "{field.err.size}")
    private String firstName;

    @NotNull(message = "{field.err.null}")
    @Size(min = 3, max = 64, message = "{field.err.size}")
    private String lastName;

    @Pattern(regexp = "(\\w)+@([a-z])+\\.com", message = "{field.err.email}")
    @Size(min = 3, max = 64, message = "{field.err.size}")
    private String email;

    @NotNull(message = "{field.err.null}")
    @Pattern(regexp = "[+]\\d{12}$", message = "{field.err.phone}")
    private String phone;

}

package com.estore.dto.response;

import com.estore.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDto that = (UserResponseDto) o;
        return username.equals(that.username) && password.equals(that.password) && role == that.role && firstName.equals(that.firstName) && lastName.equals(that.lastName) && email.equals(that.email) && phone.equals(that.phone) && Objects.equals(address, that.address) && Objects.equals(ordersHistory, that.ordersHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, role, firstName, lastName, email, phone, address, ordersHistory);
    }
}

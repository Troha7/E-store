package com.estore.dto.response;

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

    private String name;

    private String email;

    private String phone;

    private String password;

    private AddressResponseDto address;

    private List<OrderResponseDto> ordersHistory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResponseDto that = (UserResponseDto) o;
        return name.equals(that.name) && email.equals(that.email) && phone.equals(that.phone) && password.equals(that.password) && Objects.equals(address, that.address) && Objects.equals(ordersHistory, that.ordersHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, phone, password, address, ordersHistory);
    }
}

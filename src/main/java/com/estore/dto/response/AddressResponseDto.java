package com.estore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressResponseDto that = (AddressResponseDto) o;
        return city.equals(that.city) && street.equals(that.street) && house.equals(that.house);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, street, house);
    }
}

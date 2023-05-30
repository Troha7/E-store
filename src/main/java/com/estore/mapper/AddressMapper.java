package com.estore.mapper;

import com.estore.dto.request.AddressRequestDto;
import com.estore.dto.response.AddressResponseDto;
import com.estore.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@link AddressMapper}
 *
 * @author Dmytro Trotsenko on 5/30/23
 */

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponseDto toDto(Address address);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    Address toModel(AddressRequestDto addressRequestDto);

}

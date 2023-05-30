package com.estore.mapper;

import com.estore.dto.request.UserRequestDto;
import com.estore.dto.response.UserResponseDto;
import com.estore.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@link UserMapper}
 *
 * @author Dmytro Trotsenko on 5/30/23
 */

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "ordersHistory", ignore = true)
    @Mapping(target = "address", ignore = true)
    UserResponseDto toUser(User user);

    @Mapping(target = "id", ignore = true)
    User toModel(UserRequestDto userRequestDto);

}

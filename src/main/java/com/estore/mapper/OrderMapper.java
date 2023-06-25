package com.estore.mapper;

import com.estore.dto.request.OrderRequestDto;
import com.estore.dto.response.OrderResponseDto;
import com.estore.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@link OrderMapper}
 *
 * @author Dmytro Trotsenko on 5/30/23
 */

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    OrderResponseDto toDto(Order order);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "id", ignore = true)
    Order toModel(OrderRequestDto orderRequestDto);

    Order toModel(OrderResponseDto orderResponseDto);

}

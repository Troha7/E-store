package com.estore.mapper;

import com.estore.dto.request.OrderItemRequestDto;
import com.estore.dto.response.OrderItemResponseDto;
import com.estore.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@link OrderItemMapper}
 *
 * @author Dmytro Trotsenko on 5/30/23
 */

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "product", ignore = true)
    OrderItemResponseDto toDto(OrderItem orderItem);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "id", ignore = true)
    OrderItem toModel(OrderItemRequestDto orderItemRequestDto);

}

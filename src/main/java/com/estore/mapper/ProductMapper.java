package com.estore.mapper;

import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * {@link ProductMapper}
 *
 * @author Dmytro Trotsenko on 5/30/23
 */

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductResponseDto toDto(Product product);

    @Mapping(target = "id", ignore = true)
    Product toModel(ProductRequestDto productRequestDto);

}

package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.CartItemDTO;
import com.gdcp.guangdongmuseumshop.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.InheritInverseConfiguration;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {}
)
public interface CartItemMapper {
    @Mapping(source = "id", target = "itemId")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.mainImage", target = "productImage")
    @Mapping(source = "product.price", target = "productPrice")
    @Mapping(source = "product.stock", target = "stock")
    CartItemDTO toDTO(CartItem entity);

    @InheritInverseConfiguration
    CartItem toEntity(CartItemDTO dto);
}
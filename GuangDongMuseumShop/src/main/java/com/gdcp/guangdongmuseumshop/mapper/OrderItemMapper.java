package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.OrderItemDTO;
import com.gdcp.guangdongmuseumshop.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderItemMapper {
    
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.image", target = "productImage")
    @Mapping(source = "price", target = "productPrice")
    @Mapping(source = "order.id", target = "orderId")
    OrderItemDTO toDTO(OrderItem orderItem);

    @Mapping(target = "product", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(source = "productPrice", target = "price")
    OrderItem toEntity(OrderItemDTO orderItemDTO);

    List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);
} 
package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.OrderDTO;
import com.gdcp.guangdongmuseumshop.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {OrderItemMapper.class}
)
public interface OrderMapper {
    
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "orderNo", target = "orderNumber")
    @Mapping(source = "trackingNo", target = "trackingNumber")
    @Mapping(source = "orderItems", target = "orderItems")
    OrderDTO toDTO(Order order);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(source = "orderNumber", target = "orderNo")
    @Mapping(source = "trackingNumber", target = "trackingNo")
    Order toEntity(OrderDTO orderDTO);

    List<OrderDTO> toDTOList(List<Order> orders);
} 
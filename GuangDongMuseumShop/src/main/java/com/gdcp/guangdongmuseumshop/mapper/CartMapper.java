package com.gdcp.guangdongmuseumshop.mapper;

import com.gdcp.guangdongmuseumshop.dto.CartDTO;
import com.gdcp.guangdongmuseumshop.entity.Cart;
import com.gdcp.guangdongmuseumshop.enums.CartStatus;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = {CartItemMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CartMapper {
    @Named("statusToString")
    default String statusToString(CartStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToStatus")
    default CartStatus stringToStatus(String status) {
        return status != null ? CartStatus.valueOf(status) : null;
    }

    @Mapping(target = "userId", expression = "java(cart.getUser() != null ? cart.getUser().getId() : null)")
    @Mapping(target = "username", expression = "java(cart.getUser() != null ? cart.getUser().getUsername() : null)")
    @Mapping(target = "status", qualifiedByName = "statusToString")
    CartDTO toDTO(Cart cart);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", qualifiedByName = "stringToStatus")
    Cart toEntity(CartDTO cartDTO);
} 
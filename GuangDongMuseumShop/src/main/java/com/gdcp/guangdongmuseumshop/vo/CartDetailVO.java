package com.gdcp.guangdongmuseumshop.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CartDetailVO extends CartVO {
    private List<CartItemVO> items;
} 
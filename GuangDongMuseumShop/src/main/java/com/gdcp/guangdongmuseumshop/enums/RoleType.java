package com.gdcp.guangdongmuseumshop.enums;

public enum RoleType {
    ADMIN("ADMIN", "管理员"),
    USER("USER", "普通用户");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
} 
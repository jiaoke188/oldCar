package com.example.carrentalapp.data;

public final class UserRole {

    public static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static final String MERCHANT = "MERCHANT";
    public static final String CUSTOMER = "CUSTOMER";

    private UserRole() {
    }

    public static String toDisplay(String role) {
        if (SUPER_ADMIN.equals(role)) {
            return "超级管理员";
        }
        if (MERCHANT.equals(role)) {
            return "商家";
        }
        if (CUSTOMER.equals(role)) {
            return "客户";
        }
        return role;
    }
}

package com.example.projet_ing1;

public class Session {
    private static int userId;

    public static void setUserId(int id) {
        userId = id;
    }

    public static int getUserId() {
        return userId;
    }

    public static void clear() {
        userId = -1; // ou tout autre reset
    }
    private static String role;

    public static void setUserRole(String r) {
        role = r;
    }

    public static String getUserRole() {
        return role;
    }

}
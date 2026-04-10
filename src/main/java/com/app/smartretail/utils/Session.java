package com.app.smartretail.utils;

import com.app.smartretail.model.User;

/**
 * Session - menyimpan informasi user yang sedang login
 */
public class Session {

    public static User currentUser = null;

    private Session() {}

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(String... roles) {
        if (currentUser == null) return false;
        for (String r : roles) {
            if (currentUser.getRole().equalsIgnoreCase(r)) return true;
        }
        return false;
    }

    public static boolean isAdmin()        { return hasRole("ADMIN"); }
    public static boolean isKasir()        { return hasRole("KASIR"); }
    public static boolean isStaffGudang()  { return hasRole("STAFF_GUDANG"); }
    public static boolean isSupervisor()   { return hasRole("SUPERVISOR"); }

    public static void logout() {
        currentUser = null;
    }
}

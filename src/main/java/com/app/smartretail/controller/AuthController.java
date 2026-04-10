package com.app.smartretail.controller;

import com.app.smartretail.dao.UserDAO;
import com.app.smartretail.model.User;
import com.app.smartretail.utils.PasswordHasher;
import com.app.smartretail.utils.Session;

public class AuthController {

    private UserDAO userDAO;

    public AuthController() {
        this.userDAO = new UserDAO();
    }

    /**
     * Login: verifikasi username + password
     * @return User jika berhasil, null jika gagal
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        String hashed = PasswordHasher.hash(password);
        User user = userDAO.login(username.trim(), hashed);
        if (user != null) {
            Session.currentUser = user;
            System.out.println("[Auth] Login berhasil: " + user.getNamaLengkap() + " [" + user.getRole() + "]");
        }
        return user;
    }

    public void logout() {
        System.out.println("[Auth] Logout: " + (Session.currentUser != null ? Session.currentUser.getUsername() : "-"));
        Session.logout();
    }

    public boolean changePassword(String passwordLama, String passwordBaru) {
        if (Session.currentUser == null) return false;
        if (!PasswordHasher.verify(passwordLama, Session.currentUser.getPassword())) return false;
        boolean ok = userDAO.updatePassword(Session.currentUser.getId(), PasswordHasher.hash(passwordBaru));
        if (ok) Session.currentUser.setPassword(PasswordHasher.hash(passwordBaru));
        return ok;
    }
}

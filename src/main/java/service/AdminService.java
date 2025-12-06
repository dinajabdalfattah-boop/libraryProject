package service;

import domain.Admin;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling administrator authentication and management.
 * Supports:
 * - US1.1  (Admin login)
 * - US1.2  (Admin logout)
 */
public class AdminService {

    /** List of all registered admins */
    private final List<Admin> admins = new ArrayList<>();

    /** Currently logged-in admin (null if none) */
    private Admin loggedInAdmin = null;

    /**
     * Adds a new admin if both ID and username are unique.
     */
    public boolean addAdmin(String userName, int adminId, String password) {

        // Prevent duplicate adminId or duplicate username
        for (Admin a : admins) {
            if (a.getAdminId() == adminId || a.getUserName().equals(userName)) {
                return false;
            }
        }

        admins.add(new Admin(userName, adminId, password));
        return true;
    }

    /**
     * Attempts to log in an administrator.
     * Only one admin may be logged in at a time.
     */
    public boolean login(String userName, String password) {

        // Prevent multiple admins logged in at once
        if (loggedInAdmin != null) {
            return false;
        }

        for (Admin a : admins) {
            // Let Admin object validate credentials
            if (a.login(userName, password)) {
                loggedInAdmin = a;
                return true;
            }
        }

        return false;
    }

    /**
     * Logs out the currently logged-in admin.
     */
    public void logout() {
        if (loggedInAdmin != null) {
            loggedInAdmin.logout();  // mark admin as logged out
            loggedInAdmin = null;
        }
    }

    /** @return true if an admin is logged in */
    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null && loggedInAdmin.isLoggedIn();
    }

    /** @return currently logged-in admin or null */
    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    /** @return all registered admins */
    public List<Admin> getAllAdmins() {
        return admins;
    }
}

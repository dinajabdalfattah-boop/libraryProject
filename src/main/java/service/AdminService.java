package service;

import domain.Admin;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for handling administrator authentication and management.
 * Covers:
 * - Adding admin accounts
 * - Login / logout operations
 * - Fetching admin information
 *
 * Supports US1.1 (Admin login) and US1.2 (Admin logout)
 */
public class AdminService {

    /** List of all registered admins */
    private final List<Admin> admins = new ArrayList<>();

    /** Currently logged-in admin (null if none) */
    private Admin loggedInAdmin = null;

    /**
     * Adds a new admin if the ID is unique.
     *
     * @param userName admin username
     * @param adminId unique admin ID
     * @param password admin password
     * @return true if added, false if ID is duplicate
     */
    public boolean addAdmin(String userName, int adminId, String password) {
        for (Admin a : admins) {
            if (a.getAdminId() == adminId) {
                return false;
            }
        }
        admins.add(new Admin(userName, adminId, password));
        return true;
    }

    /**
     * Attempts to log in an administrator.
     * Only one admin may be logged in at a time.
     *
     * @param userName admin username
     * @param password admin password
     * @return true if login successful, false otherwise
     */
    public boolean login(String userName, String password) {
        if (loggedInAdmin != null) {
            return false;
        }

        for (Admin a : admins) {
            if (a.getUserName().equals(userName) &&
                    a.getPassword().equals(password)) {
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
        loggedInAdmin = null;
    }

    /**
     * @return true if an admin is logged in
     */
    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null;
    }

    /**
     * @return the currently logged-in admin, or null if none
     */
    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    /**
     * @return list of all registered admins
     */
    public List<Admin> getAllAdmins() {
        return admins;
    }
}

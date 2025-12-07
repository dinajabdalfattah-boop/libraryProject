package service;

import domain.Admin;
import java.util.ArrayList;
import java.util.List;

/**
 * This service class manages all administrator-related operations in the system.
 * It supports registering new admins, logging in, logging out, and checking
 * who is currently logged in. Only one administrator can be logged in at any time.
 *
 * Features implemented according to:
 * - US1.1: Admin login
 * - US1.2: Admin logout
 */
public class AdminService {

    private final List<Admin> admins = new ArrayList<>();
    private Admin loggedInAdmin = null;

    /**
     * Registers a new administrator in the system.
     * The admin is only added if both the username and admin ID are unique.
     *
     * @param userName the username for the new admin
     * @param adminId  a unique numerical identifier for the admin
     * @param password the password used for authentication
     * @return true if the admin was successfully added, false if ID or username already exists
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
     * Attempts to log an administrator into the system.
     * Only one admin may be logged in at any time.
     *
     * @param userName the admin's username
     * @param password the admin's password
     * @return true if login is successful, false otherwise
     */
    public boolean login(String userName, String password) {
        if (loggedInAdmin != null) {
            return false;
        }

        for (Admin a : admins) {
            if (a.login(userName, password)) {
                loggedInAdmin = a;
                return true;
            }
        }

        return false;
    }

    /**
     * Logs out the currently logged-in administrator.
     * If no admin is logged in, the method does nothing.
     */
    public void logout() {
        if (loggedInAdmin != null) {
            loggedInAdmin.logout();
            loggedInAdmin = null;
        }
    }

    /**
     * Checks whether an administrator is currently logged in.
     *
     * @return true if an admin is logged in, otherwise false
     */
    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null && loggedInAdmin.isLoggedIn();
    }

    /**
     * Returns the admin who is currently logged in.
     *
     * @return the logged-in admin, or null if none
     */
    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    /**
     * Returns a list of all administrators registered in the system.
     *
     * @return a list containing all Admin objects
     */
    public List<Admin> getAllAdmins() {
        return admins;
    }
}

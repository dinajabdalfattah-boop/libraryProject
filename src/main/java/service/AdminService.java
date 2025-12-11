package service;

import domain.Admin;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This service class manages all administrator-related operations in the system.
 * It supports:
 *  - registering new admins
 *  - loading/saving admins from/to a file
 *  - logging in / out
 *  - checking who is currently logged in
 *
 * File format (admins.txt):
 *   userName,adminId,password
 */
public class AdminService {

    private final List<Admin> admins = new ArrayList<>();
    private Admin loggedInAdmin = null;

    private static final String ADMINS_FILE = "src/main/resources/data/admins.txt";

    /**
     * Registers a new administrator in the system.
     * The admin is only added if both the username and admin ID are unique.
     * On success the updated list is saved to the file.
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
        saveAdminsToFile();
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

    // -------------------- File persistence --------------------

    /**
     * Saves all admins to the admins.txt file.
     * Format of each line:
     *   userName,adminId,password
     */
    private void saveAdminsToFile() {
        List<String> lines = new ArrayList<>();

        for (Admin a : admins) {
            String line = a.getUserName() + "," +
                    a.getAdminId() + "," +
                    a.getPassword();
            lines.add(line);
        }

        FileManager.writeLines(ADMINS_FILE, lines);
    }

    /**
     * Loads admins from the admins.txt file.
     * This method is defensive:
     *  - ignores null/blank lines
     *  - ignores malformed lines (missing fields)
     *  - ignores lines with non-numeric IDs
     */
    public void loadAdminsFromFile() {
        admins.clear();

        List<String> lines = FileManager.readLines(ADMINS_FILE);
        if (lines == null) {
            return;
        }

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            String[] p = line.split(",");
            if (p.length < 3) {
                continue;
            }

            String userName = p[0];
            String idStr = p[1];
            String password = p[2];

            int adminId;
            try {
                adminId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                // invalid id, skip this line
                continue;
            }

            Admin admin = new Admin(userName, adminId, password);
            admins.add(admin);
        }
    }
}

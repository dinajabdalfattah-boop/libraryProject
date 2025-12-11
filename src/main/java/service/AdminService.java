package service;

import domain.Admin;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing administrator operations:
 * - add admin
 * - login / logout
 * - load from file
 * - save to file
 */
public class AdminService {

    private final List<Admin> admins = new ArrayList<>();
    private Admin loggedInAdmin = null;

    private static final String ADMINS_FILE = "src/main/resources/data/admins.txt";

    /**
     * Adds a new admin only if username and adminId are unique.
     * Automatically saves the updated list to file.
     */
    public boolean addAdmin(String userName, int adminId, String password) {

        for (Admin a : admins) {
            if (a.getAdminId() == adminId || a.getUserName().equals(userName)) {
                return false;
            }
        }

        admins.add(new Admin(userName, adminId, password));
        saveAdminsToFile();     // 🔥 حفظ تلقائي
        return true;
    }

    /**
     * Attempts to log in an administrator.
     */
    public boolean login(String userName, String password) {

        if (loggedInAdmin != null) {
            return false; // someone already logged in
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
     * Logs out the current admin.
     */
    public void logout() {
        if (loggedInAdmin != null) {
            loggedInAdmin.logout();
            loggedInAdmin = null;
        }
    }

    /**
     * Returns true if an admin is logged in.
     */
    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null && loggedInAdmin.isLoggedIn();
    }

    /**
     * Gets the logged-in admin instance.
     */
    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    /**
     * Returns all registered admins.
     */
    public List<Admin> getAllAdmins() {
        return admins;
    }

    // ---------------------------------------------------
    //  FILE OPERATIONS
    // ---------------------------------------------------

    /**
     * Saves all admins to file.
     * Format: userName,adminId,password
     */
    public void saveAdminsToFile() {   // 🔥 public الآن
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
     * Loads admins from file (ignores invalid lines).
     */
    public void loadAdminsFromFile() {

        admins.clear();

        List<String> lines = FileManager.readLines(ADMINS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 3) continue;

            String userName = p[0];
            String idStr = p[1];
            String password = p[2];

            int adminId;
            try {
                adminId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                continue; // skip malformed line
            }

            admins.add(new Admin(userName, adminId, password));
        }
    }
}

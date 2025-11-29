package service;

import domain.Admin;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    private final List<Admin> admins = new ArrayList<>();
    private Admin loggedInAdmin = null;

    public boolean addAdmin(String userName, int adminId, String password) {
        for (Admin a : admins) {
            if (a.getAdminId() == adminId) {
                return false;
            }
        }
        Admin newAdmin = new Admin(userName, adminId, password);
        admins.add(newAdmin);
        return true;
    }

    public boolean login(String userName, String password) {
        if (loggedInAdmin != null) {
            return false;
        }

        for (Admin a : admins) {
            if (a.getUserName().equals(userName) && a.getPassword().equals(password)) {
                loggedInAdmin = a;
                return true;
            }
        }
        return false;
    }

    public void logout() {
        loggedInAdmin = null;
    }

    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null;
    }

    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    public List<Admin> getAllAdmins() {
        return admins;
    }
}

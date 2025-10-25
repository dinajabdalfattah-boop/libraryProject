package service;
import domain.Admin;
import java.util.ArrayList;

public class AdminService {


    private final ArrayList<Admin> admins = new ArrayList<>();


    private Admin loggedInAdmin = null;

    public void addAdmin(String userName, int adminId, String password) {
        for (Admin a : admins) {
            if (a.getAdminId() == adminId) {
                System.out.println(" Admin with ID " + adminId + " already exists!");
                return;
            }
        }
        Admin newAdmin = new Admin(userName, adminId, password);
        admins.add(newAdmin);
        System.out.println(" Admin added successfully: " + userName);
    }

    public boolean login(String userName, String password) {
        if (loggedInAdmin != null) {
            System.out.println(" An admin is already logged in: " + loggedInAdmin.getUserName());
            return false;
        }

        for (Admin a : admins) {
            if (a.getUserName().equals(userName) && a.getPassword().equals(password)) {
                loggedInAdmin = a;
                System.out.println(" Login successful. Welcome, " + a.getUserName() + "!");
                return true;
            }
        }
        System.out.println(" Invalid username or password!");
        return false;
    }

    // عملية تسجيل الخروج
    public void logout() {
        if (loggedInAdmin == null) {
            System.out.println(" No admin is currently logged in.");
        } else {
            System.out.println(" Admin " + loggedInAdmin.getUserName() + " logged out successfully.");
            loggedInAdmin = null;
        }
    }

    public boolean isAdminLoggedIn() {
        return loggedInAdmin != null;
    }

    public Admin getLoggedInAdmin() {
        return loggedInAdmin;
    }

    public void showAllAdmins() {
        if (admins.isEmpty()) {
            System.out.println(" No admins in the system yet.");
        } else {
            System.out.println(" All Admins:");
            for (Admin a : admins) {
                System.out.println(" ID: " + a.getAdminId() + " , Username: " + a.getUserName());
            }
        }
    }
}

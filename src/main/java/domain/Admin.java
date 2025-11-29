package domain;

/**
 * Represents an administrator in the Library Management System.
 * Admins can log in and manage users and books.
 */
public class Admin {

    /** Username of the admin. */
    private String userName;

    /** Unique admin ID (immutable). */
    private final int adminId;

    /** Admin password. */
    private String password;

    /**
     * Creates a new Admin.
     *
     * @param userName the admin username
     * @param adminId  unique ID of the admin
     * @param password the admin password
     */
    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    // -------- Getters --------

    public String getUserName() {
        return userName;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getPassword() {
        return password;
    }

    // -------- Setters --------

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // -------- Utility --------

    @Override
    public String toString() {
        return "Admin{" +
                "userName='" + userName + '\'' +
                ", adminId=" + adminId +
                '}';
    }
}

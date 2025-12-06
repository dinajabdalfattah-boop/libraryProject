package domain;

/**
 * Represents an administrator in the Library Management System.
 */
public class Admin {

    private String userName;
    private final int adminId;
    private String password;

    /** Tracks login state of the admin. */
    private boolean loggedIn = false;

    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    // -------- Authentication --------

    /**
     * Attempts to log in the admin using username and password.
     *
     * @return true if credentials match, false otherwise.
     */
    public boolean login(String userName, String password) {
        if (this.userName.equals(userName) && this.password.equals(password)) {
            this.loggedIn = true;
            return true;
        }
        return false;
    }

    /**
     * Logs out the admin.
     */
    public void logout() {
        this.loggedIn = false;
    }

    /**
     * @return true if admin is logged in.
     */
    public boolean isLoggedIn() {
        return loggedIn;
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

    @Override
    public String toString() {
        return "Admin{" +
                "userName='" + userName + '\'' +
                ", adminId=" + adminId +
                ", loggedIn=" + loggedIn +
                '}';
    }
}

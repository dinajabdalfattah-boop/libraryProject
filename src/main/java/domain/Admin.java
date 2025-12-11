package domain;

/**
 * Represents an administrator in the Library Management System.
 * This class stores admin credentials and manages authentication
 * such as login and logout operations.
 */
public class Admin {

    private String userName;
    private final int adminId;
    private String password;
    private boolean loggedIn = false;

    /**
     * Constructs a new Admin with the specified username, ID, and password.
     *
     * @param userName the administrator username
     * @param adminId the unique identifier of the administrator
     * @param password the administrator password
     */
    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    /**
     * Authenticates the administrator using the provided credentials.
     * If the username and password are correct, the admin is logged in.
     *
     * @param userName the entered username
     * @param password the entered password
     * @return true if authentication succeeds, false otherwise
     */
    public boolean login(String userName, String password) {
        if (this.userName.equals(userName) && this.password.equals(password)) {
            this.loggedIn = true;
            return true;
        }
        return false;
    }

    /**
     * Logs the administrator out of the system.
     */
    public void logout() {
        this.loggedIn = false;
    }

    /**
     * Checks whether the administrator is currently logged in.
     *
     * @return true if the admin is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Returns the administrator username.
     *
     * @return the admin username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the administrator ID.
     *
     * @return the unique admin ID
     */
    public int getAdminId() {
        return adminId;
    }

    /**
     * Returns the administrator password.
     *
     * @return the admin password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Updates the administrator username.
     *
     * @param userName the new username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Updates the administrator password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a string representation of the admin object.
     *
     * @return a formatted string containing admin details
     */
    @Override
    public String toString() {
        return "Admin{" +
                "userName='" + userName + '\'' +
                ", adminId=" + adminId +
                ", loggedIn=" + loggedIn +
                '}';
    }
}

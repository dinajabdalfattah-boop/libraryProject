package domain;

/**
 * This class represents an Admin user in the Library Management System.
 * The admin is responsible for performing system-level operations,
 * and this class mainly handles authentication and basic admin details.
 */
public class Admin {


    private String userName;
    private final int adminId;
    private String password;
    private boolean loggedIn = false;

    /**
     * Creates a new Admin object with the given username, ID, and password.
     *
     * @param userName the admin's username
     * @param adminId the unique ID of the admin
     * @param password the admin's password
     */
    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    // -------- Authentication --------

    /**
     * Tries to log the admin into the system by checking the given credentials.
     *
     * @param userName the username entered by the user
     * @param password the password entered by the user
     * @return true if both the username and password match the stored values,
     *         otherwise false
     */
    public boolean login(String userName, String password) {
        if (this.userName.equals(userName) && this.password.equals(password)) {
            this.loggedIn = true;
            return true;
        }
        return false;
    }

    /**
     * Logs the admin out of the system by resetting the login state.
     */
    public void logout() {
        this.loggedIn = false;
    }

    /**
     * Checks whether the admin is currently logged in.
     *
     * @return true if the admin is logged in, otherwise false
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * @return the admin's username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the unique ID of the admin
     */
    public int getAdminId() {
        return adminId;
    }

    /**
     * @return the admin's password
     */
    public String getPassword() {
        return password;
    }

    // -------- Setters --------

    /**
     * Updates the admin's username.
     *
     * @param userName the new username to be set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Updates the admin's password.
     *
     * @param password the new password to be set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns a readable string that represents the admin object,
     * mostly for debugging and display purposes.
     *
     * @return a string showing the admin's information
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

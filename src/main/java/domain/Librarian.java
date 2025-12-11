package domain;

/**
 * This class represents a Librarian in the library system.
 *
 * A librarian is responsible for operational tasks such as:
 *  - detecting overdue books (US 2.2 requirement)
 *  - detecting overdue CDs
 *  - viewing overdue items counts
 *
 * Librarians support basic authentication (login/logout),
 * but they do NOT have administrative privileges
 * such as registering or removing users.
 *
 * The class stores:
 *  - librarian ID (unique)
 *  - name
 *  - password
 *  - login state
 */
public class Librarian {

    private final int librarianId;
    private String name;
    private String password;
    private boolean loggedIn = false;

    /**
     * Creates a new librarian with an ID, name, and password.
     *
     * @param librarianId unique identifier for the librarian
     * @param name the librarian's name
     * @param password used for login authentication
     */
    public Librarian(int librarianId, String name, String password) {
        this.librarianId = librarianId;
        this.name = name;
        this.password = password;
    }

    // -------------------- Authentication --------------------

    /**
     * Attempts to log the librarian in using the provided credentials.
     *
     * @param name the name entered during login
     * @param password the password entered during login
     * @return true if credentials match, false otherwise
     */
    public boolean login(String name, String password) {
        if (this.name.equals(name) && this.password.equals(password)) {
            loggedIn = true;
            return true;
        }
        return false;
    }

    /**
     * Logs the librarian out by resetting the login state.
     */
    public void logout() {
        loggedIn = false;
    }

    /**
     * @return true if the librarian is currently logged in
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    // -------------------- Getters --------------------

    /**
     * @return the librarian's unique ID
     */
    public int getLibrarianId() {
        return librarianId;
    }

    /**
     * @return the librarian's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the librarian's password.
     * This is used by the LibrarianService to save data to a file.
     *
     * @return the password string
     */
    public String getPassword() {
        return password;
    }

    // -------------------- Setters --------------------

    /**
     * Updates the librarian's password.
     *
     * @param password new password to replace the old one
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return readable representation of the librarian
     */
    @Override
    public String toString() {
        return "Librarian[id=" + librarianId +
                ", name=" + name +
                ", loggedIn=" + loggedIn + "]";
    }
}

package service;

import domain.User;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This service handles all user-related operations in the library system.
 * It supports:
 * - registering new users
 * - saving and loading user data from a file
 * - searching for users
 * - checking borrowing eligibility (Sprint 4)
 * - unregistering users safely
 *
 * All users are stored in an in-memory list and synchronized with a file.
 */
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "src/main/resources/data/users.txt";

    /**
     * Adds a new user with the given name and email.
     * The method allows null values, but prevents duplicate names
     * (case-insensitive).
     *
     * @param name  the user's name
     * @param email the user's email address
     * @return true if the user was successfully added, false if duplicate name
     */
    public boolean addUser(String name, String email) {

        for (User u : users) {
            if (u.getUserName() != null &&
                    name != null &&
                    u.getUserName().equalsIgnoreCase(name)) {
                return false;
            }
        }

        User user = new User(name, email);
        users.add(user);
        saveUsers();

        return true;
    }

    /**
     * Overloaded version used mainly for tests.
     * Creates a user with a default email value.
     *
     * @param name user name
     * @return true if added successfully
     */
    public boolean addUser(String name) {
        return addUser(name, "no-email@none.com");
    }

    /**
     * Saves all current users to the storage file.
     * Each user is written as:
     * name,email,fineBalance
     *
     * Null emails are written as the string "null".
     */
    private void saveUsers() {
        List<String> lines = new ArrayList<>();

        for (User u : users) {
            String line = u.getUserName() + "," +
                    (u.getEmail() == null ? "null" : u.getEmail()) + "," +
                    u.getFineBalance();
            lines.add(line);
        }

        FileManager.writeLines(USERS_FILE, lines);
    }

    /**
     * Loads user data from the file and reconstructs the users list.
     * This method is designed to be fully safe, avoiding:
     * - null pointer issues
     * - incomplete lines
     * - invalid number formats
     *
     * Special handling:
     * - "null" email string → real null
     * - malformed fine values default to 0.0
     */
    public void loadUsersFromFile() {

        users.clear();

        List<String> lines = FileManager.readLines(USERS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank())
                continue;

            String[] p = line.split(",");

            if (p.length < 3)
                continue;

            String name = p[0];

            String email;
            if (p[1].equals("null")) {
                email = null;
            } else {
                email = p[1];
            }

            double fine = 0.0;
            try {
                fine = Double.parseDouble(p[2]);
            } catch (Exception e) {
                fine = 0.0;
            }

            User u = new User(name, email);
            u.setFineBalance(fine);

            users.add(u);
        }
    }

    /**
     * Searches for a user by name (case-insensitive).
     *
     * @param name the name to search for
     * @return the matching User or null if none found
     */
    public User findUserByName(String name) {
        if (name == null) return null;

        for (User u : users) {
            if (u.getUserName() != null &&
                    u.getUserName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }

    /**
     * Returns a copy of the full user list.
     *
     * @return a new List containing all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Checks whether a user is eligible to borrow an item.
     * Rules:
     * - user must not be null
     * - no unpaid fines
     * - no overdue loans
     *
     * @param user the user to check
     * @return true if user is allowed to borrow
     */
    public boolean canBorrow(User user) {

        if (user == null)
            return false;

        if (user.getFineBalance() > 0)
            return false;

        return !user.hasOverdueLoans();
    }

    /**
     * Attempts to unregister a user from the system.
     * A user can only be unregistered if:
     * - they have no active loans
     * - they owe no outstanding fines
     *
     * @param user the user to remove
     * @return true if removed successfully, false otherwise
     */
    public boolean unregisterUser(User user) {

        if (user == null) return false;

        if (!user.canBeUnregistered()) {
            return false;
        }

        return users.remove(user);
    }
}

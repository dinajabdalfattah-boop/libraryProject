package service;

import domain.User;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing users in the library system.
 * This class supports adding users, saving/loading users from storage,
 * finding users, checking borrowing eligibility, and unregistering users.
 */
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "src/main/resources/data/users.txt";

    /**
     * Adds a new user to the system if the username does not already exist
     * (case-insensitive). After a successful insert, data is persisted to file.
     *
     * @param name the user name
     * @param email the user email address
     * @return true if the user was added successfully, false otherwise
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
     * Adds a new user with a default email address.
     *
     * @param name the user name
     * @return true if the user was added successfully, false otherwise
     */
    public boolean addUser(String name) {
        return addUser(name, "no-email@none.com");
    }

    /**
     * Saves all users to the storage file using a comma-separated format:
     * name,email,fineBalance
     * If the email is null, it is stored as "null".
     */
    public void saveUsers() {

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
     * Loads all users from the storage file into memory.
     * Invalid or incomplete lines are ignored.
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
     * Finds a user by name (case-insensitive).
     *
     * @param name the user name to search for
     * @return the matching user, or null if not found
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
     * Returns a copy of all users currently loaded in memory.
     *
     * @return a list containing all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Checks whether the given user can borrow new items.
     * A user cannot borrow if they have unpaid fines or any overdue loans.
     *
     * @param user the user to check
     * @return true if the user can borrow, false otherwise
     */
    public boolean canBorrow(User user) {

        if (user == null)
            return false;

        if (user.getFineBalance() > 0)
            return false;

        return !user.hasOverdueLoans();
    }

    /**
     * Unregisters (removes) a user from the system if they meet the rules:
     * - no unpaid fines
     * - no active loans (books or CDs)
     *
     * If the user is removed successfully, the updated list is saved to storage.
     *
     * @param user the user to remove
     * @return true if the user was removed successfully, false otherwise
     */
    public boolean unregisterUser(User user) {

        if (user == null) return false;

        if (!user.canBeUnregistered()) {
            return false;
        }

        boolean removed = users.remove(user);

        if (removed) {
            saveUsers();
        }

        return removed;
    }

}

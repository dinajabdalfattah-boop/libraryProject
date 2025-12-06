package service;

import domain.User;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles user registration, lookup, persistence, and borrowing eligibility.
 */
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "src/main/resources/data/users.txt";

    // ---------------------------------------------------------
    // User Creation
    // ---------------------------------------------------------

    /** Add user with name + email */
    public boolean addUser(String name, String email) {

        // allow null name or email, but prevent duplicates
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

    /** Version for tests */
    public boolean addUser(String name) {
        return addUser(name, "no-email@none.com");
    }

    // ---------------------------------------------------------
    // Save users to file
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // Load users from file (fully safe)
    // ---------------------------------------------------------

    /** ✔ النسخة النهائية – آمنة بالكامل */
    public void loadUsersFromFile() {

        users.clear();

        List<String> lines = FileManager.readLines(USERS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank())
                continue;

            String[] p = line.split(",");

            // حماية من ArrayIndexOutOfBounds
            if (p.length < 3)
                continue;

            // name
            String name = p[0];

            // email (الفرق الوحيد الذي طلبته)
            // "" يبقى كما هو
            // "null" → null
            String email;
            if (p[1].equals("null")) {
                email = null;
            } else {
                email = p[1];   // يسمح بالقيم "", " ", إلخ
            }

            // fine parsing (safe)
            double fine = 0.0;
            try {
                fine = Double.parseDouble(p[2]);
            } catch (Exception e) {
                fine = 0.0; // لو "notNumber"
            }

            User u = new User(name, email);
            u.setFineBalance(fine);

            users.add(u);
        }
    }

    // ---------------------------------------------------------
    // Finders
    // ---------------------------------------------------------

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

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // ---------------------------------------------------------
    // Borrow Eligibility (Sprint 4)
    // ---------------------------------------------------------

    /** ✔ النسخة المعدّلة لإزالة NullPointerException */
    public boolean canBorrow(User user) {

        if (user == null)
            return false;

        if (user.getFineBalance() > 0)
            return false;

        return !user.hasOverdueLoans();
    }

    // ---------------------------------------------------------
    // Unregister User (Sprint 4)
    // ---------------------------------------------------------

    public boolean unregisterUser(User user) {

        if (user == null) return false;

        if (!user.canBeUnregistered()) {
            return false;
        }

        // remove will fail if the given object instance is not the SAME instance
        return users.remove(user);
    }
}

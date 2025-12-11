package service;

import domain.User;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This service handles all user-related operations in the library system.
 */
public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "src/main/resources/data/users.txt";

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
        saveUsers();   // 🔥 حفظ تلقائي

        return true;
    }

    public boolean addUser(String name) {
        return addUser(name, "no-email@none.com");
    }

    /**
     * Changed to PUBLIC so Main/admin can call it 🔥
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

    public boolean canBorrow(User user) {

        if (user == null)
            return false;

        if (user.getFineBalance() > 0)
            return false;

        return !user.hasOverdueLoans();
    }

    public boolean unregisterUser(User user) {

        if (user == null) return false;

        // لا يمكن حذف مستخدم عنده لون active أو عليه fine
        if (!user.canBeUnregistered()) {
            return false;
        }

        boolean removed = users.remove(user);

        if (removed) {
            saveUsers(); // احفظ التغيير في الفايل
        }

        return removed;
    }

}

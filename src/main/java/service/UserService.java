package service;

import domain.User;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final List<User> users = new ArrayList<>();
    private static final String USERS_FILE = "src/main/resources/data/users.txt";

    /** NEW version: add user with email */
    public boolean addUser(String name, String email) {
        for (User u : users) {
            if (u.getUserName().equalsIgnoreCase(name)) return false;
        }

        User user = new User(name, email);
        users.add(user);
        saveUsers();
        return true;
    }

    /** OLD version used by TESTS */
    public boolean addUser(String name) {
        return addUser(name, "no-email@none.com");
    }

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

    public void loadUsersFromFile() {
        users.clear();

        List<String> lines = FileManager.readLines(USERS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            String name = p[0];
            String email = p[1].equals("null") ? null : p[1];
            double fine = Double.parseDouble(p[2]);

            User u = new User(name, email);
            u.setFineBalance(fine);

            users.add(u);
        }
    }

    public User findUserByName(String name) {
        for (User u : users)
            if (u.getUserName().equalsIgnoreCase(name))
                return u;
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users); // expected by tests
    }

    public boolean canBorrow(User user) {
        return user.getFineBalance() <= 0; // expected by tests
    }
}

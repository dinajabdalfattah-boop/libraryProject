package service;

import domain.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public boolean addUser(String name) {
        for (User u : users) {
            if (u.getUserName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        User newUser = new User(name);
        users.add(newUser);
        return true;
    }

    public User findUserByName(String name) {
        for (User u : users) {
            if (u.getUserName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public boolean canBorrow(User user) {
        return user.getFineBalance() <= 0;
    }
}

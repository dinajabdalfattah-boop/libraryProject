package service;

import domain.User;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final List<User> users = new ArrayList<>();

    public void addUser(String name) {
        User newUser = new User(name);
        users.add(newUser);
        System.out.println("User added successfully: " + name);
    }

    public User findUserByName(String name) {
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        System.out.println("User not found: " + name);
        return null;
    }

    public void showAllUsers() {
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("All Users:");
            for (User u : users) {
                System.out.println("Name: " + u.getName() + ", Fine Balance: " + u.getFineBalance());
            }
        }
    }

    public boolean canBorrow(User user) {
        if (user.getFineBalance() > 0) {
            System.out.println("User " + user.getName() + " cannot borrow books. Outstanding fine: " + user.getFineBalance());
            return false;
        }
        return true;
    }
}

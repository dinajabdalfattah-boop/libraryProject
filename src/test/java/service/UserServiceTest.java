package service;

import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        System.setOut(new PrintStream(outContent));
    }

    private void resetOutput() { outContent.reset(); }

    @Test
    public void testAddUserSuccess() {
        userService.addUser("Alice");
        String output = outContent.toString();
        assertTrue(output.contains("User added successfully"));
        User user = userService.findUserByName("Alice");
        assertNotNull(user);
        resetOutput();
    }

    @Test
    public void testFindUserByNameExists() {
        userService.addUser("Bob");
        resetOutput();
        User user = userService.findUserByName("Bob");
        assertNotNull(user);
        resetOutput();
    }

    @Test
    public void testFindUserByNameNotExists() {
        User user = userService.findUserByName("Nonexistent");
        assertNull(user);
        assertTrue(outContent.toString().contains("User not found"));
        resetOutput();
    }

    @Test
    public void testCanBorrowNoFine() {
        userService.addUser("Charlie");
        User user = userService.findUserByName("Charlie");
        assertTrue(userService.canBorrow(user));
        resetOutput();
    }

    @Test
    public void testCanBorrowWithFine() {
        userService.addUser("David");
        User user = userService.findUserByName("David");
        user.setFineBalance(20.0);
        assertFalse(userService.canBorrow(user));
        assertTrue(outContent.toString().contains("cannot borrow books"));
        resetOutput();
    }

    @Test
    public void testShowAllUsersEmpty() {
        userService.showAllUsers();
        assertTrue(outContent.toString().contains("No users found"));
        resetOutput();
    }

    @Test
    public void testShowAllUsersNonEmpty() {
        userService.addUser("Eve");
        resetOutput();
        userService.showAllUsers();
        String output = outContent.toString();
        assertTrue(output.contains("All Users") && output.contains("Eve"));
        resetOutput();
    }

    @Test
    public void testMultipleUsersAndFines() {
        userService.addUser("Frank");
        userService.addUser("Grace");
        User frank = userService.findUserByName("Frank");
        User grace = userService.findUserByName("Grace");
        frank.setFineBalance(10);
        grace.setFineBalance(0);

        List<User> users = List.of(frank, grace);
        for (User u : users) {
            if (u.getFineBalance() > 0) {
                assertFalse(userService.canBorrow(u));
            } else {
                assertTrue(userService.canBorrow(u));
            }
        }
    }
}

package service;

import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    private String name1;
    private String name2;

    @BeforeEach
    public void setUp() {
        userService = new UserService();

        name1 = "UserA";
        name2 = "UserB";
    }

    @Test
    public void testAddUserSuccess() {
        assertTrue(userService.addUser(name1));

        User u = userService.findUserByName(name1);
        assertNotNull(u);
        assertEquals(name1, u.getUserName());
    }

    @Test
    public void testAddUserDuplicateFails() {
        assertTrue(userService.addUser(name1));
        assertFalse(userService.addUser(name1));

        List<User> all = userService.getAllUsers();
        assertEquals(1, all.size());
    }

    @Test
    public void testFindUserExists() {
        userService.addUser(name1);
        User u = userService.findUserByName(name1);
        assertNotNull(u);
    }

    @Test
    public void testFindUserNotExists() {
        assertNull(userService.findUserByName("Unknown"));
    }

    @Test
    public void testGetAllUsersEmpty() {
        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    public void testGetAllUsersNonEmpty() {
        userService.addUser(name1);
        userService.addUser(name2);
        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    public void testCanBorrowNoFine() {
        userService.addUser(name1);
        User u = userService.findUserByName(name1);
        assertTrue(userService.canBorrow(u));
    }

    @Test
    public void testCanBorrowWithFine() {
        userService.addUser(name1);
        User u = userService.findUserByName(name1);
        u.setFineBalance(20);

        assertFalse(userService.canBorrow(u));
    }
    @Test
    public void testAddUserWithEmail() {
        assertTrue(userService.addUser("MailUser", "m@mail.com"));

        User u = userService.findUserByName("MailUser");
        assertNotNull(u);
        assertEquals("m@mail.com", u.getEmail());
    }

    @Test
    public void testAddUserDuplicateIgnoreCase() {
        assertTrue(userService.addUser("Ali"));
        assertFalse(userService.addUser("ali")); // equalsIgnoreCase
    }

    @Test
    public void testLoadUsersFromFile() {
        List<String> lines = new ArrayList<>();
        lines.add("Loaded1,l1@mail.com,5.0");
        lines.add("Loaded2,null,0.0");

        // نكتب الملف زي ما يتوقع UserService
        FileManager.writeLines("src/main/resources/data/users.txt", lines);

        userService.loadUsersFromFile();

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());

        User u1 = userService.findUserByName("loaded1");
        assertNotNull(u1);
        assertEquals("l1@mail.com", u1.getEmail());
        assertEquals(5.0, u1.getFineBalance());

        User u2 = userService.findUserByName("Loaded2");
        assertNotNull(u2);
        assertNull(u2.getEmail());
        assertEquals(0.0, u2.getFineBalance());
    }

    @Test
    public void testMultipleUsersBorrowLogic() {
        userService.addUser(name1);
        userService.addUser(name2);

        User u1 = userService.findUserByName(name1);
        User u2 = userService.findUserByName(name2);

        u1.setFineBalance(10);
        u2.setFineBalance(0);

        assertFalse(userService.canBorrow(u1));
        assertTrue(userService.canBorrow(u2));
    }
}

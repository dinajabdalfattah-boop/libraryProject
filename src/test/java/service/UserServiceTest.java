package service;

import domain.Book;
import domain.CDLoan;
import domain.Loan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        FileManager.writeLines("src/main/resources/data/users.txt", new ArrayList<>());
    }

    // ---------------------------------------------------------
    // ADD USER TESTS
    // ---------------------------------------------------------

    @Test
    public void testAddUserSuccess() {
        assertTrue(userService.addUser("UserA"));
        assertNotNull(userService.findUserByName("UserA"));
    }

    @Test
    public void testAddUserDuplicateFails() {
        userService.addUser("UserA");
        assertFalse(userService.addUser("UserA"));
    }

    @Test
    public void testAddUserDuplicateIgnoreCaseFails() {
        userService.addUser("Ali");
        assertFalse(userService.addUser("ali"));
    }

    @Test
    public void testAddUserWithEmail() {
        userService.addUser("MailUser", "m@mail.com");
        assertEquals("m@mail.com", userService.findUserByName("MailUser").getEmail());
    }

    @Test
    public void testAddUserWithNullEmail() {
        userService.addUser("NullEmailUser", null);
        assertNull(userService.findUserByName("NullEmailUser").getEmail());
    }

    @Test
    public void testAddUserWithNullNameDoesNotCrash() {
        assertDoesNotThrow(() -> userService.addUser(null, "x@mail.com"));
    }

    // ---------------------------------------------------------
    // FINDERS
    // ---------------------------------------------------------

    @Test
    public void testFindUserExists() {
        userService.addUser("A");
        assertNotNull(userService.findUserByName("A"));
    }

    @Test
    public void testFindUserNotExists() {
        assertNull(userService.findUserByName("Ghost"));
    }

    @Test
    public void testGetAllUsersEmpty() {
        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    public void testGetAllUsersNonEmpty() {
        userService.addUser("A");
        userService.addUser("B");
        assertEquals(2, userService.getAllUsers().size());
    }

    // ---------------------------------------------------------
    // LOAD USERS FROM FILE
    // ---------------------------------------------------------

    @Test
    public void testLoadUsersFromFileValid() {
        List<String> lines = List.of(
                "User1,u1@mail.com,5.0",
                "User2,null,0.0",
                ""
        );

        FileManager.writeLines("src/main/resources/data/users.txt", lines);
        userService.loadUsersFromFile();

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    public void testLoadUsersFromFileMissingFields() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("BadLine"));

        assertDoesNotThrow(() -> userService.loadUsersFromFile());
        assertEquals(0, userService.getAllUsers().size());
    }

    @Test
    public void testLoadUsersFromFileFineIsInvalidString() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("UserA,a@mail.com,notNumber"));

        assertDoesNotThrow(() -> userService.loadUsersFromFile());

        User u = userService.findUserByName("UserA");
        assertNotNull(u);
        assertEquals(0.0, u.getFineBalance()); // from catch
    }

    @Test
    public void testLoadUsersFromFileEmailEmptyString() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("UserA,,3.0"));

        userService.loadUsersFromFile();

        User u = userService.findUserByName("UserA");
        assertNotNull(u);
        assertEquals("", u.getEmail());
        assertEquals(3.0, u.getFineBalance());
    }

    @Test
    public void testLoadUsersFromLineSpacesOnly() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("   "));

        assertDoesNotThrow(() -> userService.loadUsersFromFile());
        assertEquals(0, userService.getAllUsers().size());
    }

    // ---------------------------------------------------------
    // CAN BORROW
    // ---------------------------------------------------------

    @Test
    public void testCanBorrowUserNullReturnsFalse() {
        assertFalse(userService.canBorrow(null));
    }

    @Test
    public void testCanBorrowNoFine() {
        userService.addUser("A");
        assertTrue(userService.canBorrow(userService.findUserByName("A")));
    }

    @Test
    public void testCanBorrowFailsDueToFine() {
        userService.addUser("A");
        User u = userService.findUserByName("A");
        u.setFineBalance(10);
        assertFalse(userService.canBorrow(u));
    }

    @Test
    public void testCanBorrowFailsDueToOverdueLoan() {
        userService.addUser("A");
        User u = userService.findUserByName("A");

        Loan l = new Loan(u, new Book("T", "A", "111"));
        l.setDueDate(LocalDate.now().minusDays(3));

        u.getActiveBookLoans().add(l);

        assertFalse(userService.canBorrow(u));
    }

    // ---------------------------------------------------------
    // UNREGISTER USER
    // ---------------------------------------------------------

    @Test
    public void testUnregisterUserSuccess() {
        userService.addUser("A");
        assertTrue(userService.unregisterUser(userService.findUserByName("A")));
    }

    @Test
    public void testUnregisterUserFailsDueToFine() {
        userService.addUser("A");
        User u = userService.findUserByName("A");
        u.setFineBalance(10);
        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void testUnregisterUserFailsDueToActiveBookLoan() {
        userService.addUser("A");
        User u = userService.findUserByName("A");
        u.getActiveBookLoans().add(new Loan(u, new Book("T","A","111")));

        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void testUnregisterUserFailsDueToActiveCDLoan() {
        userService.addUser("A");
        User u = userService.findUserByName("A");
        u.getActiveCDLoans().add(new CDLoan(u, new domain.CD("C","X","1")));

        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void testUnregisterUserNull() {
        assertFalse(userService.unregisterUser(null));
    }

    @Test
    public void testUnregisterUserNotInList() {
        assertFalse(userService.unregisterUser(new User("Ghost")));
    }

    @Test
    public void testUnregisterUserFailsBecauseDifferentInstance() {
        userService.addUser("A");
        User fake = new User("A", "xx@mail.com");
        assertFalse(userService.unregisterUser(fake));
    }

    @Test
    public void testUnregisterUserWithNullName() {
        User u = new User(null, "x@mail.com");
        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void testUnregisterUserHasBothActiveBookAndCDLoans() {
        userService.addUser("A");
        User u = userService.findUserByName("A");

        u.getActiveBookLoans().add(new Loan(u, new Book("T","A","111")));
        u.getActiveCDLoans().add(new CDLoan(u, new domain.CD("C","X","1")));

        assertFalse(userService.unregisterUser(u));
    }
}

package service;

import domain.Book;
import domain.CD;
import domain.CDLoan;
import domain.Loan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
        FileManager.writeLines("src/main/resources/data/users.txt", new ArrayList<>());
    }

    @Test
    public void addUser_success() {
        assertTrue(userService.addUser("UserA", "a@mail.com"));
        assertNotNull(userService.findUserByName("UserA"));
    }

    @Test
    public void addUser_duplicate_ignoreCase_fails() {
        assertTrue(userService.addUser("Ali", "a@mail.com"));
        assertFalse(userService.addUser("ali", "b@mail.com"));
    }

    @Test
    public void addUser_existingUserNameNull_allowsSameName() {
        assertTrue(userService.addUser(null, "x@mail.com"));
        assertTrue(userService.addUser("Same", "s@mail.com"));
        assertFalse(userService.addUser("same", "t@mail.com"));
        assertNotNull(userService.findUserByName("Same"));
        assertNotNull(userService.findUserByName("same"));
    }

    @Test
    public void addUser_overload_defaultEmail() {
        assertTrue(userService.addUser("NoMail"));
        User u = userService.findUserByName("NoMail");
        assertNotNull(u);
        assertEquals("no-email@none.com", u.getEmail());
    }

    @Test
    public void saveUsers_writesNullEmailAsNullLiteral() {
        userService.addUser("U1", null);
        List<String> lines = FileManager.readLines("src/main/resources/data/users.txt");
        assertFalse(lines.isEmpty());
        assertTrue(lines.get(0).startsWith("U1,null,"));
    }

    @Test
    public void loadUsersFromFile_linesNull_returnsWithoutCrash() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);
            assertDoesNotThrow(() -> userService.loadUsersFromFile());
            assertTrue(userService.getAllUsers().isEmpty());
        }
    }

    @Test
    public void loadUsersFromFile_skipsBlankAndShortLines() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("", "   ", "BadLine", "A,a@mail.com,1.0"));
        userService.loadUsersFromFile();
        assertEquals(1, userService.getAllUsers().size());
        assertNotNull(userService.findUserByName("A"));
    }

    @Test
    public void loadUsersFromFile_emailNull_andFineOk() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("User2,null,0.0"));
        userService.loadUsersFromFile();
        User u = userService.findUserByName("User2");
        assertNotNull(u);
        assertNull(u.getEmail());
        assertEquals(0.0, u.getFineBalance());
    }

    @Test
    public void loadUsersFromFile_invalidFine_setsZero() {
        FileManager.writeLines("src/main/resources/data/users.txt",
                List.of("UserA,a@mail.com,notNumber"));
        userService.loadUsersFromFile();
        User u = userService.findUserByName("UserA");
        assertNotNull(u);
        assertEquals(0.0, u.getFineBalance());
    }

    @Test
    public void findUserByName_null_returnsNull() {
        assertNull(userService.findUserByName(null));
    }

    @Test
    public void findUserByName_existingUserNameNull_isIgnored() {
        userService.addUser(null, "x@mail.com");
        userService.addUser("Ali", "a@mail.com");
        assertNotNull(userService.findUserByName("Ali"));
        assertNull(userService.findUserByName(""));
    }

    @Test
    public void getAllUsers_returnsCopy() {
        userService.addUser("A", "a@mail.com");
        List<User> copy = userService.getAllUsers();
        assertEquals(1, copy.size());
        copy.clear();
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    public void canBorrow_userNull_false() {
        assertFalse(userService.canBorrow(null));
    }

    @Test
    public void canBorrow_finePositive_false() {
        User u = new User("U", "u@mail.com");
        u.setFineBalance(1);
        assertFalse(userService.canBorrow(u));
    }

    @Test
    public void canBorrow_overdue_truePath_and_falsePath() {
        User u = new User("U", "u@mail.com");
        u.setFineBalance(0);
        assertTrue(userService.canBorrow(u));

        Loan overdue = new Loan(u, new Book("T", "A", "111"));
        overdue.setDueDate(java.time.LocalDate.now().minusDays(2));
        u.getActiveBookLoans().add(overdue);

        assertFalse(userService.canBorrow(u));
    }

    @Test
    public void unregisterUser_null_false() {
        assertFalse(userService.unregisterUser(null));
    }

    @Test
    public void unregisterUser_success_saves() {
        userService.addUser("A", "a@mail.com");
        User u = userService.findUserByName("A");
        assertNotNull(u);
        assertTrue(userService.unregisterUser(u));
        assertNull(userService.findUserByName("A"));
    }

    @Test
    public void unregisterUser_notInList_false() {
        assertFalse(userService.unregisterUser(new User("Ghost", "g@mail.com")));
    }

    @Test
    public void unregisterUser_fineBlocks_false() {
        userService.addUser("A", "a@mail.com");
        User u = userService.findUserByName("A");
        u.setFineBalance(10);
        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void unregisterUser_activeBookLoanBlocks_false() {
        userService.addUser("A", "a@mail.com");
        User u = userService.findUserByName("A");
        u.getActiveBookLoans().add(new Loan(u, new Book("T", "A", "111")));
        assertFalse(userService.unregisterUser(u));
    }

    @Test
    public void unregisterUser_activeCDLoanBlocks_false() {
        userService.addUser("A", "a@mail.com");
        User u = userService.findUserByName("A");
        u.getActiveCDLoans().add(new CDLoan(u, new CD("C", "X", "1")));
        assertFalse(userService.unregisterUser(u));
    }
}

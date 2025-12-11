package domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LibrarianTest {

    @Test
    void testInitialState() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        assertEquals(1, librarian.getLibrarianId());
        assertEquals("Sara", librarian.getName());
        assertEquals("1234", librarian.getPassword());
        assertFalse(librarian.isLoggedIn());
        assertTrue(librarian.toString().contains("Librarian"));
    }

    @Test
    void testLoginSuccess() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        boolean result = librarian.login("Sara", "1234");

        assertTrue(result);
        assertTrue(librarian.isLoggedIn());
    }

    @Test
    void testLoginWrongName() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        boolean result = librarian.login("WrongName", "1234");

        assertFalse(result);
        assertFalse(librarian.isLoggedIn());
    }

    @Test
    void testLoginWrongPassword() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        boolean result = librarian.login("Sara", "wrong");

        assertFalse(result);
        assertFalse(librarian.isLoggedIn());
    }

    @Test
    void testLoginWrongNameAndPassword() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        boolean result = librarian.login("Other", "wrong");

        assertFalse(result);
        assertFalse(librarian.isLoggedIn());
    }

    @Test
    void testLogout() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        librarian.login("Sara", "1234");
        assertTrue(librarian.isLoggedIn());

        librarian.logout();
        assertFalse(librarian.isLoggedIn());
    }

    @Test
    void testSetPasswordThenLogin() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        librarian.setPassword("newPass");

        assertTrue(librarian.login("Sara", "newPass"));
        assertFalse(librarian.login("Sara", "1234")); // old password no longer valid
    }

    @Test
    void testToStringContainsFields() {
        Librarian librarian = new Librarian(1, "Sara", "1234");

        String s = librarian.toString();

        assertTrue(s.contains("id=1"));
        assertTrue(s.contains("Sara"));
        assertTrue(s.contains("loggedIn="));
    }
}

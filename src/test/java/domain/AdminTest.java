package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private Admin admin;

    @BeforeEach
    public void setUp() {
        admin = new Admin("adminUser", 1, "password123");
    }

    // --------------------- Constructor & Getters ---------------------

    @Test
    public void testConstructorAndGetters() {
        assertEquals("adminUser", admin.getUserName());
        assertEquals(1, admin.getAdminId());
        assertEquals("password123", admin.getPassword());
        assertFalse(admin.isLoggedIn());
    }

    // --------------------- Login Logic ---------------------

    @Test
    public void testLoginSuccess() {
        boolean result = admin.login("adminUser", "password123");
        assertTrue(result);
        assertTrue(admin.isLoggedIn());
    }

    @Test
    public void testLoginFailWrongUsername() {
        boolean result = admin.login("wrongName", "password123");
        assertFalse(result);
        assertFalse(admin.isLoggedIn());
    }

    @Test
    public void testLoginFailWrongPassword() {
        boolean result = admin.login("adminUser", "wrongPass");
        assertFalse(result);
        assertFalse(admin.isLoggedIn());
    }

    @Test
    public void testLoginFailCompletelyWrong() {
        boolean result = admin.login("x", "y");
        assertFalse(result);
        assertFalse(admin.isLoggedIn());
    }

    // --------------------- Logout Logic ---------------------

    @Test
    public void testLogout() {
        admin.login("adminUser", "password123");
        assertTrue(admin.isLoggedIn());

        admin.logout();
        assertFalse(admin.isLoggedIn());
    }

    // --------------------- Setters ---------------------

    @Test
    public void testSetUserName() {
        admin.setUserName("newAdmin");
        assertEquals("newAdmin", admin.getUserName());
    }

    @Test
    public void testSetUserNameToNull() {
        admin.setUserName(null);
        assertNull(admin.getUserName());
    }

    @Test
    public void testSetPassword() {
        admin.setPassword("newPass");
        assertEquals("newPass", admin.getPassword());
    }

    @Test
    public void testSetPasswordEmpty() {
        admin.setPassword("");
        assertEquals("", admin.getPassword());
    }

    // --------------------- toString ---------------------

    @Test
    public void testToStringContainsFields() {
        String text = admin.toString();
        assertTrue(text.contains("adminUser"));
        assertTrue(text.contains("1"));
        assertTrue(text.contains("loggedIn=false"));
    }

    @Test
    public void testToStringAfterLogin() {
        admin.login("adminUser", "password123");
        String text = admin.toString();
        assertTrue(text.contains("loggedIn=true"));
    }

    // --------------------- Final ID ---------------------

    @Test
    public void testAdminIdIsFinal() {
        assertEquals(1, admin.getAdminId());
    }
}

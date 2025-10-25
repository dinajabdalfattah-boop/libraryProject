package domain;

import domain.Admin;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DTest {

    private Admin admin;

    @BeforeEach
    public void setUp() {
        admin = new Admin("adminUser", 1, "password123");
    }

    // Test getters
    @Test
    public void testGetters() {
        assertEquals("adminUser", admin.getUserName());
        assertEquals(1, admin.getAdminId());
        assertEquals("password123", admin.getPassword());
    }

    // Test setters
    @Test
    public void testSetters() {
        admin.setUserName("newUser");
        admin.setAdminId(2);
        admin.setPassword("newPass");

        assertEquals("newUser", admin.getUserName());
        assertEquals(2, admin.getAdminId());
        assertEquals("newPass", admin.getPassword());
    }

    // Test successful login
    @Test
    public void testLoginSuccess() {
        boolean result = admin.login("adminUser", "password123");
        assertTrue(result);
    }

    // Test failed login with wrong username
    @Test
    public void testLoginFailWrongUsername() {
        boolean result = admin.login("wrongUser", "password123");
        assertFalse(result);
    }

    // Test failed login with wrong password
    @Test
    public void testLoginFailWrongPassword() {
        boolean result = admin.login("adminUser", "wrongPass");
        assertFalse(result);
    }

    // Test logout
    @Test
    public void testLogout() {
        // لا توجد حالة محفوظة بالclass حول login status، لذلك فقط تحقق من تنفيذ الوظيفة بدون استثناء
        assertDoesNotThrow(() -> admin.logout());
    }
}



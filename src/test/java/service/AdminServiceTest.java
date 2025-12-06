package service;

import domain.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    private AdminService adminService;

    private String name1;
    private String pass1;
    private int id1;

    private String name2;
    private String pass2;
    private int id2;

    @BeforeEach
    public void setUp() {
        adminService = new AdminService();

        name1 = "AdminA";
        pass1 = "PassA";
        id1 = 100;

        name2 = "AdminB";
        pass2 = "PassB";
        id2 = 200;
    }

    // ---------------------------------------------------------
    // Add Admin Tests
    // ---------------------------------------------------------

    @Test
    public void testAddAdminSuccess() {
        assertTrue(adminService.addAdmin(name1, id1, pass1));
        assertEquals(1, adminService.getAllAdmins().size());
    }

    @Test
    public void testAddAdminFailsWhenDuplicateId() {
        assertTrue(adminService.addAdmin(name1, id1, pass1));

        // duplicate id
        assertFalse(adminService.addAdmin("AnotherName", id1, "newPass"));
    }

    @Test
    public void testAddAdminFailsWhenDuplicateUsername() {
        assertTrue(adminService.addAdmin(name1, id1, pass1));

        // duplicate username
        assertFalse(adminService.addAdmin(name1, 9999, "randomPass"));
    }

    // ---------------------------------------------------------
    // Login Tests
    // ---------------------------------------------------------

    @Test
    public void testLoginSuccess() {
        adminService.addAdmin(name1, id1, pass1);

        assertTrue(adminService.login(name1, pass1));
        assertTrue(adminService.isAdminLoggedIn());

        Admin logged = adminService.getLoggedInAdmin();
        assertNotNull(logged);
        assertEquals(name1, logged.getUserName());
        assertTrue(logged.isLoggedIn());
    }

    @Test
    public void testLoginFailsWrongPassword() {
        adminService.addAdmin(name1, id1, pass1);

        assertFalse(adminService.login(name1, "WrongPass"));
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testLoginFailsWrongUsername() {
        adminService.addAdmin(name1, id1, pass1);

        assertFalse(adminService.login("Unknown", pass1));
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testLoginFailsWhenAlreadyLoggedIn() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.addAdmin(name2, id2, pass2);

        assertTrue(adminService.login(name1, pass1));

        // logging in again should fail
        assertFalse(adminService.login(name2, pass2));
        assertTrue(adminService.isAdminLoggedIn());
    }

    // ---------------------------------------------------------
    // Logout Tests
    // ---------------------------------------------------------

    @Test
    public void testLogoutSuccess() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.login(name1, pass1);

        adminService.logout();

        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    @Test
    public void testLogoutWhenNoAdminLoggedIn() {
        adminService.logout(); // should not crash
        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    // ---------------------------------------------------------
    // Getters Tests
    // ---------------------------------------------------------

    @Test
    public void testGetLoggedInAdminReturnsCorrectObject() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.login(name1, pass1);

        Admin admin = adminService.getLoggedInAdmin();
        assertNotNull(admin);
        assertEquals(name1, admin.getUserName());
        assertEquals(id1, admin.getAdminId());
    }

    @Test
    public void testIsAdminLoggedInFalseByDefault() {
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testGetAllAdmins() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.addAdmin(name2, id2, pass2);

        assertEquals(2, adminService.getAllAdmins().size());
    }
}

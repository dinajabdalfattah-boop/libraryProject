package domain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.AdminService;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    private AdminService adminService;

    @BeforeEach
    public void setUp() {
        adminService = new AdminService();
        adminService.addAdmin("adminUser", 1, "password123");
    }

    // Test successful login
    @Test
    public void testLoginSuccess() {
        boolean result = adminService.login("adminUser", "password123");
        assertTrue(result);
        assertTrue(adminService.isAdminLoggedIn());
    }

    // Test failed login
    @Test
    public void testLoginFail() {
        boolean result = adminService.login("wrongUser", "password123");
        assertFalse(result);
        assertFalse(adminService.isAdminLoggedIn());
    }

    // Test logout
    @Test
    public void testLogout() {
        adminService.login("adminUser", "password123");
        assertTrue(adminService.isAdminLoggedIn());
        adminService.logout();
        assertFalse(adminService.isAdminLoggedIn());
    }

    // Test show all admins
    @Test
    public void testShowAllAdmins() {
        adminService.showAllAdmins();
    }
}

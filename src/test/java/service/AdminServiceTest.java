package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminServiceTest {

    private AdminService adminService;

    @BeforeEach
    public void setUp() {
        adminService = new AdminService();
    }

    // ======== Test adding admin when list is empty ========
    @Test
    public void testAddAdminWhenListEmpty() {
        adminService.addAdmin("adminUser", 1, "password123");
        assertTrue(adminService.login("adminUser", "password123"));
        assertTrue(adminService.isAdminLoggedIn());
    }

    // ======== Test adding duplicate Admin ID ========
    @Test
    public void testAddAdminDuplicateId() {
        adminService.addAdmin("adminUser", 1, "password123");
        adminService.addAdmin("anotherAdmin", 1, "pass123"); // duplicate
        // Login with duplicate should fail
        boolean loginDuplicate = adminService.login("anotherAdmin", "pass123");
        assertFalse(loginDuplicate);
        // Original admin still works
        assertTrue(adminService.login("adminUser", "password123"));
    }

    // ======== Test successful login ========
    @Test
    public void testLoginSuccess() {
        adminService.addAdmin("adminUser", 1, "password123");
        boolean result = adminService.login("adminUser", "password123");
        assertTrue(result);
        assertTrue(adminService.isAdminLoggedIn());
    }

    // ======== Test failed login (wrong credentials) ========
    @Test
    public void testLoginFailWrongCredentials() {
        adminService.addAdmin("adminUser", 1, "password123");
        boolean result = adminService.login("wrongUser", "password123");
        assertFalse(result);
        assertFalse(adminService.isAdminLoggedIn());
    }

    // ======== Test login when already logged in ========
    @Test
    public void testLoginWhenAlreadyLoggedIn() {
        adminService.addAdmin("adminUser", 1, "password123");
        // First login
        assertTrue(adminService.login("adminUser", "password123"));
        // Attempt second login while already logged in
        boolean result = adminService.login("adminUser", "password123");
        assertFalse(result);
        assertTrue(adminService.isAdminLoggedIn());
    }

    // ======== Test logout when admin is logged in ========
    @Test
    public void testLogoutWhenAdminLoggedIn() {
        adminService.addAdmin("adminUser", 1, "password123");
        adminService.login("adminUser", "password123");
        assertTrue(adminService.isAdminLoggedIn());
        adminService.logout();
        assertFalse(adminService.isAdminLoggedIn());
    }

    // ======== Test logout when no admin is logged in ========
    @Test
    public void testLogoutWhenNoAdminLoggedIn() {
        // logout without login
        adminService.logout();
        assertFalse(adminService.isAdminLoggedIn());
    }

    // ======== Test show all admins (visual check) ========
    @Test
    public void testShowAllAdmins() {
        adminService.addAdmin("adminUser", 1, "password123");
        adminService.showAllAdmins(); // output should show one admin
    }
}

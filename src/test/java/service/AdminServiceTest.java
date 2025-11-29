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

    @Test
    public void testAddAdminAndLogin() {
        adminService.addAdmin(name1, id1, pass1);

        assertTrue(adminService.login(name1, pass1));
        assertTrue(adminService.isAdminLoggedIn());
    }

    @Test
    public void testAddDuplicateAdminIdFails() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.addAdmin(name2, id1, pass2);

        assertFalse(adminService.login(name2, pass2));

        // original admin still works
        assertTrue(adminService.login(name1, pass1));
    }

    @Test
    public void testLoginSuccess() {
        adminService.addAdmin(name1, id1, pass1);
        assertTrue(adminService.login(name1, pass1));
    }

    @Test
    public void testLoginWrongPassword() {
        adminService.addAdmin(name1, id1, pass1);
        assertFalse(adminService.login(name1, "WrongPass"));
    }

    @Test
    public void testLoginWrongUsername() {
        adminService.addAdmin(name1, id1, pass1);
        assertFalse(adminService.login("Unknown", pass1));
    }

    @Test
    public void testLoginWhenAlreadyLoggedIn() {
        adminService.addAdmin(name1, id1, pass1);
        assertTrue(adminService.login(name1, pass1));

        assertFalse(adminService.login(name1, pass1));
        assertTrue(adminService.isAdminLoggedIn());
    }

    @Test
    public void testLogout() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.login(name1, pass1);

        adminService.logout();
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testLogoutWhenNotLoggedIn() {
        adminService.logout();
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testIsAdminLoggedInDefaultFalse() {
        assertFalse(adminService.isAdminLoggedIn());
    }

    @Test
    public void testGetLoggedInAdmin() {
        adminService.addAdmin(name1, id1, pass1);
        adminService.login(name1, pass1);

        Admin loggedIn = adminService.getLoggedInAdmin();
        assertNotNull(loggedIn);
        assertEquals(name1, loggedIn.getUserName());
    }
}

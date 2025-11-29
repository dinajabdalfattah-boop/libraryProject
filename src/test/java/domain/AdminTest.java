package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    private Admin admin;
    private String initialName;
    private int initialId;
    private String initialPassword;

    @BeforeEach
    public void setUp() {
        initialName = "adminUser";
        initialId = 1;
        initialPassword = "password123";

        admin = new Admin(initialName, initialId, initialPassword);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(initialName, admin.getUserName());
        assertEquals(initialId, admin.getAdminId());
        assertEquals(initialPassword, admin.getPassword());
    }

    @Test
    public void testSetUserName() {
        String newName = "newName";
        admin.setUserName(newName);
        assertEquals(newName, admin.getUserName());
    }

    @Test
    public void testSetPassword() {
        String newPass = "newPass123";
        admin.setPassword(newPass);
        assertEquals(newPass, admin.getPassword());
    }

    @Test
    public void testFullUpdate() {
        String newName = "megaAdmin";
        String newPassword = "ultraPass";

        admin.setUserName(newName);
        admin.setPassword(newPassword);

        assertEquals(newName, admin.getUserName());
        assertEquals(newPassword, admin.getPassword());
        assertEquals(initialId, admin.getAdminId());
    }

    @Test
    public void testAdminIdIsFinal() {
        assertEquals(initialId, admin.getAdminId());
    }


    @Test
    public void testSetUserNameToNull() {
        admin.setUserName(null);
        assertNull(admin.getUserName());
    }

    @Test
    public void testSetPasswordToEmptyString() {
        String emptyPass = "";
        admin.setPassword(emptyPass);
        assertEquals(emptyPass, admin.getPassword());
    }

    @Test
    public void testStringContainsUsernameAndId() {
        String representation = "Admin: " + admin.getUserName() + " (ID: " + admin.getAdminId() + ")";
        assertTrue(representation.contains(admin.getUserName()));
        assertTrue(representation.contains(String.valueOf(admin.getAdminId())));
    }
}

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

    @Test
    public void testGetters() {
        assertEquals("adminUser", admin.getUserName());
        assertEquals(1, admin.getAdminId());
        assertEquals("password123", admin.getPassword());
    }

    @Test
    public void testSetUserName() {
        admin.setUserName("newAdmin");
        assertEquals("newAdmin", admin.getUserName());
    }

    @Test
    public void testSetAdminId() {
        admin.setAdminId(99);
        assertEquals(99, admin.getAdminId());
    }

    @Test
    public void testSetPassword() {
        admin.setPassword("newPass");
        assertEquals("newPass", admin.getPassword());
    }

    @Test
    public void testFullUpdate() {
        // تحديث كل الخصائص معاً
        admin.setUserName("superAdmin");
        admin.setAdminId(42);
        admin.setPassword("superPass");

        assertEquals("superAdmin", admin.getUserName());
        assertEquals(42, admin.getAdminId());
        assertEquals("superPass", admin.getPassword());
    }

    @Test
    public void testToStringRepresentation() {
        // إذا أردت يمكن إضافة toString لكلاس Admin لاحقاً
        // هنا فقط نضمن الكفرج لكل الخصائص
        String repr = "Username: " + admin.getUserName() + ", ID: " + admin.getAdminId();
        assertTrue(repr.contains(admin.getUserName()));
        assertTrue(repr.contains(String.valueOf(admin.getAdminId())));
    }
}


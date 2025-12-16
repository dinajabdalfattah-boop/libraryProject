package service;

import domain.Admin;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AdminServiceTest {

    private AdminService adminService;

    @BeforeEach
    public void setUp() {
        adminService = new AdminService();
    }

    @Test
    public void addAdmin_success_savesToFile() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(adminService.addAdmin("AdminA", 100, "PassA"));
            assertEquals(1, adminService.getAllAdmins().size());
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    public void addAdmin_fails_duplicateId_noSave() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(adminService.addAdmin("AdminA", 100, "PassA"));
            assertFalse(adminService.addAdmin("AdminB", 100, "PassB"));
            assertEquals(1, adminService.getAllAdmins().size());
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    public void addAdmin_fails_duplicateUsername_noSave() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(adminService.addAdmin("AdminA", 100, "PassA"));
            assertFalse(adminService.addAdmin("AdminA", 200, "PassB"));
            assertEquals(1, adminService.getAllAdmins().size());
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    public void login_success_setsLoggedInAdmin() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
        }

        assertTrue(adminService.login("AdminA", "PassA"));
        assertTrue(adminService.isAdminLoggedIn());
        assertNotNull(adminService.getLoggedInAdmin());
        assertEquals("AdminA", adminService.getLoggedInAdmin().getUserName());
    }

    @Test
    public void login_fails_wrongPassword() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
        }

        assertFalse(adminService.login("AdminA", "Wrong"));
        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    @Test
    public void login_fails_unknownUsername() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
        }

        assertFalse(adminService.login("Nope", "PassA"));
        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    @Test
    public void login_fails_whenAlreadyLoggedIn() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
            adminService.addAdmin("AdminB", 200, "PassB");
        }

        assertTrue(adminService.login("AdminA", "PassA"));
        assertFalse(adminService.login("AdminB", "PassB"));
        assertTrue(adminService.isAdminLoggedIn());
        assertEquals("AdminA", adminService.getLoggedInAdmin().getUserName());
    }

    @Test
    public void logout_whenLoggedIn_clearsState() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
        }
        adminService.login("AdminA", "PassA");

        adminService.logout();

        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    @Test
    public void logout_whenNoOneLoggedIn_doesNothing() {
        assertDoesNotThrow(() -> adminService.logout());
        assertFalse(adminService.isAdminLoggedIn());
        assertNull(adminService.getLoggedInAdmin());
    }

    @Test
    public void getAllAdmins_returnsSameListReference_andUpdates() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            List<Admin> listRef = adminService.getAllAdmins();
            assertTrue(listRef.isEmpty());

            adminService.addAdmin("AdminA", 100, "PassA");
            assertEquals(1, listRef.size());

            adminService.addAdmin("AdminB", 200, "PassB");
            assertEquals(2, listRef.size());
        }
    }

    @Test
    public void saveAdminsToFile_writesExpectedFormat() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("AdminA", 100, "PassA");
            adminService.addAdmin("AdminB", 200, "PassB");

            fm.clearInvocations();

            adminService.saveAdminsToFile();

            fm.verify(() -> FileManager.writeLines(anyString(), argThat(lines -> {
                if (lines == null || lines.size() != 2) return false;
                return lines.contains("AdminA,100,PassA") && lines.contains("AdminB,200,PassB");
            })), times(1));
        }
    }

    @Test
    public void loadAdminsFromFile_linesNull_resultsEmpty() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);

            adminService.loadAdminsFromFile();

            assertTrue(adminService.getAllAdmins().isEmpty());
        }
    }

    @Test
    public void loadAdminsFromFile_skipsInvalid_andLoadsValid() {
        List<String> lines = new ArrayList<>(Arrays.asList(
                null,
                "",
                "BadLine",
                "X,Y,Z",
                "AdminA,notNumber,PassA",
                "AdminA,100,PassA",
                "AdminB,200,PassB"
        ));

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(lines);

            adminService.loadAdminsFromFile();

            assertEquals(2, adminService.getAllAdmins().size());
            assertNotNull(adminService.getAllAdmins().get(0));
            assertNotNull(adminService.getAllAdmins().get(1));
        }
    }

    @Test
    public void loadAdminsFromFile_clearsExistingBeforeLoad() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            adminService.addAdmin("Old", 1, "P");
        }
        assertEquals(1, adminService.getAllAdmins().size());

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(List.of("AdminA,100,PassA"));

            adminService.loadAdminsFromFile();

            assertEquals(1, adminService.getAllAdmins().size());
            assertEquals("AdminA", adminService.getAllAdmins().get(0).getUserName());
        }
    }
}

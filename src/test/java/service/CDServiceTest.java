package service;

import domain.CD;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CDServiceTest {

    private CDService cdService;
    private static final String FILE = "src/main/resources/data/cds.txt";

    @BeforeEach
    void setUp() {
        cdService = new CDService();
    }

    @Test
    void addCD_success_saves() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);

            assertTrue(cdService.addCD("T1", "A1", "C1"));
            assertNotNull(cdService.findCDById("C1"));
            fm.verify(() -> FileManager.writeLines(eq(FILE), anyList()), times(1));
        }
    }

    @Test
    void addCD_duplicate_false_noSecondSave() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);

            assertTrue(cdService.addCD("T1", "A1", "C1"));
            assertFalse(cdService.addCD("T2", "A2", "C1"));
            fm.verify(() -> FileManager.writeLines(eq(FILE), anyList()), times(1));
        }
    }
    @Test
    void saveCDsToFile_writes_all_rows_including_null_dates() {
        cdService.getAllCDs().add(new CD("T1", "A1", "C1"));
        cdService.getAllCDs().add(new CD("T2", "A2", "C2"));

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);

            cdService.saveCDsToFile();

            fm.verify(() -> FileManager.writeLines(eq(FILE), argThat(list -> {
                if (list == null) return false;
                List<?> lines = (List<?>) list;
                if (lines.size() != 2) return false;

                String s1 = String.valueOf(lines.get(0));
                String s2 = String.valueOf(lines.get(1));

                return s1.equals("T1,A1,C1,true,null,null")
                        && s2.equals("T2,A2,C2,true,null,null");
            })), times(1));
        }
    }


    @Test
    void loadCDsFromFile_linesNull_noCrash() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);

            assertDoesNotThrow(() -> cdService.loadCDsFromFile());
            assertTrue(cdService.getAllCDs().isEmpty());
        }
    }

    @Test
    void loadCDsFromFile_skips_blank_and_incomplete() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(Arrays.asList(
                    null,
                    "",
                    "only,two",
                    "T1,A1,C1,true,null,null"
            ));

            cdService.loadCDsFromFile();

            assertEquals(1, cdService.getAllCDs().size());
            assertNotNull(cdService.findCDById("C1"));
        }
    }

    @Test
    void loadCDsFromFile_available_true_forces_clear_dates() {
        List<String> lines = List.of("T1,A1,C1,true,2025-01-01,2025-01-10");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(lines);

            cdService.loadCDsFromFile();

            CD cd = cdService.findCDById("C1");
            assertNotNull(cd);
            assertTrue(cd.isAvailable());
            assertNull(cd.getBorrowDate());
            assertNull(cd.getDueDate());
        }
    }

    @Test
    void loadCDsFromFile_available_null_treated_as_false_and_restores_dates_and_borrowed() {
        List<String> lines = List.of("T1,A1,C1,null,2025-01-01,2025-01-08");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(lines);

            cdService.loadCDsFromFile();

            CD cd = cdService.findCDById("C1");
            assertNotNull(cd);
            assertFalse(cd.isAvailable());
            assertEquals(LocalDate.parse("2025-01-01"), cd.getBorrowDate());
            assertEquals(LocalDate.parse("2025-01-08"), cd.getDueDate());
        }
    }

    @Test
    void loadCDsFromFile_available_false_restores_dates_and_borrowed() {
        List<String> lines = List.of("T1,A1,C1,false,2025-01-01,2025-01-08");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(lines);

            cdService.loadCDsFromFile();

            CD cd = cdService.findCDById("C1");
            assertNotNull(cd);
            assertFalse(cd.isAvailable());
            assertEquals(LocalDate.parse("2025-01-01"), cd.getBorrowDate());
            assertEquals(LocalDate.parse("2025-01-08"), cd.getDueDate());
        }
    }

    @Test
    void search_null_throws() {
        assertThrows(NullPointerException.class, () -> cdService.search(null));
    }

    @Test
    void search_blank_returns_all() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);

            cdService.addCD("Java", "Mark", "C1");
            cdService.addCD("Python", "Anna", "C2");
        }

        assertEquals(2, cdService.search("   ").size());
        assertEquals(2, cdService.search("").size());
    }

    @Test
    void search_title_contains_caseInsensitive() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);
            cdService.addCD("Best Of Java", "X", "C1");
        }
        assertEquals(1, cdService.search("java").size());
    }

    @Test
    void search_artist_equalsIgnoreCase() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);
            cdService.addCD("T1", "AnNa", "C1");
        }
        assertEquals(1, cdService.search("anna").size());
    }

    @Test
    void search_id_exact_match() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);
            cdService.addCD("T1", "A1", "C1");
        }
        assertEquals(1, cdService.search("C1").size());
    }

    @Test
    void search_no_match_empty() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);
            cdService.addCD("T1", "A1", "C1");
        }
        assertTrue(cdService.search("zzz").isEmpty());
    }

    @Test
    void findCDById_found_and_notFound() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class, CALLS_REAL_METHODS)) {
            fm.when(() -> FileManager.writeLines(anyString(), anyList())).thenAnswer(inv -> null);
            cdService.addCD("T1", "A1", "C1");
        }
        assertNotNull(cdService.findCDById("C1"));
        assertNull(cdService.findCDById("X"));
    }

    @Test
    void getAllCDs_live_list() {
        assertTrue(cdService.getAllCDs().isEmpty());
        cdService.getAllCDs().add(new CD("T", "A", "X"));
        assertEquals(1, cdService.getAllCDs().size());
    }
}

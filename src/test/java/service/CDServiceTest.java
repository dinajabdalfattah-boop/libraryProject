package service;

import domain.CD;
import file.FileManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CDServiceTest {

    @Test
    void testAddCD_Success() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            boolean added = service.addCD("Title", "Artist", "ID1");

            assertTrue(added);
            assertEquals(1, service.getAllCDs().size());
        }
    }

    @Test
    void testAddCD_DuplicateFails() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            service.addCD("T1", "A1", "ID1");
            boolean result = service.addCD("T2", "A2", "ID1");

            assertFalse(result);
            assertEquals(1, service.getAllCDs().size());
        }
    }

    @Test
    void testSearchByKeyword() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            service.addCD("Rock Album", "Queen", "CD1");
            service.addCD("Pop Hits", "Adele", "CD2");

            List<CD> result = service.search("rock");

            assertEquals(1, result.size());
            assertEquals("CD1", result.get(0).getId());
        }
    }

    @Test
    void testSearchEmptyKeywordReturnsAll() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            service.addCD("A", "B", "1");
            service.addCD("X", "Y", "2");

            List<CD> result = service.search("   ");

            assertEquals(2, result.size());
        }
    }

    @Test
    void testSearchNullKeywordThrows() {
        CDService service = new CDService();
        assertThrows(NullPointerException.class, () -> service.search(null));
    }

    @Test
    void testFindCDById() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            service.addCD("A", "B", "ID1");

            CD cd = service.findCDById("ID1");

            assertNotNull(cd);
            assertEquals("ID1", cd.getId());
        }
    }

    @Test
    void testFindCDByIdNotFound() {
        CDService service = new CDService();
        assertNull(service.findCDById("XXX"));
    }

    @Test
    void testLoadCDsFromFile() {
        CDService service = new CDService();

        List<String> fake = Arrays.asList(
                "T1,A1,ID1,true,null,null",
                "T2,A2,ID2,false,2025-01-01,2025-01-08",
                "",
                "BAD,LINE"
        );

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(fake);

            service.loadCDsFromFile();

            assertEquals(2, service.getAllCDs().size());

            CD cd1 = service.findCDById("ID1");
            assertTrue(cd1.isAvailable());

            CD cd2 = service.findCDById("ID2");
            assertFalse(cd2.isAvailable());
            assertEquals(LocalDate.parse("2025-01-01"), cd2.getBorrowDate());
        }
    }

    @Test
    void testLoadCDsFromFileNullList() {
        CDService service = new CDService();

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(null);

            service.loadCDsFromFile();

            assertEquals(0, service.getAllCDs().size());
        }
    }

    @Test
    void testLoadCDsMalformedLinesHandledSafely() {
        CDService service = new CDService();

        List<String> fake = Collections.singletonList("BAD LINE NO COMMAS");

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(fake);

            service.loadCDsFromFile();

            assertEquals(0, service.getAllCDs().size());
        }
    }
}

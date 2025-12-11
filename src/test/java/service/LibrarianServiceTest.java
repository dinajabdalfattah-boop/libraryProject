package service;

import domain.CDLoan;
import domain.Librarian;
import domain.Loan;
import file.FileManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LibrarianServiceTest {

    @Test
    void testAddLibrarianSuccessAndSaveToFile() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            boolean added = service.addLibrarian(1, "Sara", "1234");

            assertTrue(added);
            assertEquals(1, service.getAllLibrarians().size());

            mocked.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void testAddLibrarianDuplicateIdReturnsFalse() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.writeLines(anyString(), anyList()))
                    .thenAnswer(inv -> null);

            assertTrue(service.addLibrarian(1, "Sara", "1234"));
            boolean result = service.addLibrarian(1, "Other", "0000");

            assertFalse(result);
            assertEquals(1, service.getAllLibrarians().size());

            mocked.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void testLoginSuccess() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        // لا حاجة للملف هنا
        service.getAllLibrarians().add(new Librarian(1, "Sara", "1234"));

        boolean result = service.login("Sara", "1234");

        assertTrue(result);
        assertTrue(service.isLoggedIn());
        assertNotNull(service.getLoggedInLibrarian());
        assertEquals("Sara", service.getLoggedInLibrarian().getName());
    }

    @Test
    void testLoginFailWrongCredentials() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        service.getAllLibrarians().add(new Librarian(1, "Sara", "1234"));

        assertFalse(service.login("Wrong", "1234"));
        assertFalse(service.login("Sara", "Wrong"));
        assertFalse(service.isLoggedIn());
        assertNull(service.getLoggedInLibrarian());
    }

    @Test
    void testLoginWhenAlreadyLoggedInReturnsFalse() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        List<Librarian> list = service.getAllLibrarians();
        list.add(new Librarian(1, "Sara", "1234"));
        list.add(new Librarian(2, "Omar", "9999"));

        assertTrue(service.login("Sara", "1234"));
        assertFalse(service.login("Omar", "9999"));
        assertEquals("Sara", service.getLoggedInLibrarian().getName());
    }

    @Test
    void testLogoutWhenLoggedIn() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        service.getAllLibrarians().add(new Librarian(1, "Sara", "1234"));

        service.login("Sara", "1234");
        assertTrue(service.isLoggedIn());

        service.logout();

        assertFalse(service.isLoggedIn());
        assertNull(service.getLoggedInLibrarian());
    }

    @Test
    void testLogoutWhenNoOneLoggedInDoesNothing() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        assertFalse(service.isLoggedIn());
        assertNull(service.getLoggedInLibrarian());

        service.logout(); // لا يجب أن يرمي استثناء

        assertFalse(service.isLoggedIn());
        assertNull(service.getLoggedInLibrarian());
    }

    @Test
    void testGetOverdueBooksDelegatesToLibraryService() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        Loan l1 = mock(Loan.class);
        Loan l2 = mock(Loan.class);
        when(libraryService.getOverdueLoans()).thenReturn(Arrays.asList(l1, l2));

        List<Loan> overdue = service.getOverdueBooks();

        assertEquals(2, overdue.size());
        verify(libraryService, times(1)).getOverdueLoans();
    }

    @Test
    void testGetOverdueCDsDelegatesToLibraryService() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        CDLoan cd1 = mock(CDLoan.class);
        when(libraryService.getOverdueCDLoans()).thenReturn(Collections.singletonList(cd1));

        List<CDLoan> overdue = service.getOverdueCDs();

        assertEquals(1, overdue.size());
        verify(libraryService, times(1)).getOverdueCDLoans();
    }

    @Test
    void testGetTotalOverdueItems() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        when(libraryService.getOverdueLoans())
                .thenReturn(Arrays.asList(mock(Loan.class), mock(Loan.class)));
        when(libraryService.getOverdueCDLoans())
                .thenReturn(Arrays.asList(mock(CDLoan.class)));

        int total = service.getTotalOverdueItems();

        assertEquals(3, total);
    }

    @Test
    void testLoadLibrariansFromFileWithValidAndInvalidLines() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        List<String> fakeLines = Arrays.asList(
                "1,Sara,1234",
                "   ",                 // blank line
                "badLineWithoutCommas",
                "X,Name,pass",         // invalid ID
                "2,Omar,9999"
        );

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(fakeLines);

            service.loadLibrariansFromFile();

            List<Librarian> all = service.getAllLibrarians();
            assertEquals(2, all.size());

            Librarian l1 = all.get(0);
            Librarian l2 = all.get(1);

            assertEquals(1, l1.getLibrarianId());
            assertEquals("Sara", l1.getName());
            assertEquals("1234", l1.getPassword());

            assertEquals(2, l2.getLibrarianId());
            assertEquals("Omar", l2.getName());
            assertEquals("9999", l2.getPassword());
        }
    }

    @Test
    void testLoadLibrariansFromFileNullListClearsExisting() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        // data موجودة مسبقاً
        service.getAllLibrarians().add(new Librarian(1, "Old", "pass"));

        try (MockedStatic<FileManager> mocked = mockStatic(FileManager.class)) {

            mocked.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(null);

            service.loadLibrariansFromFile();

            assertTrue(service.getAllLibrarians().isEmpty());
        }
    }

    @Test
    void testGetAllLibrariansReturnsLiveListReference() {
        LibraryService libraryService = mock(LibraryService.class);
        LibrarianService service = new LibrarianService(libraryService);

        List<Librarian> first = service.getAllLibrarians();
        assertEquals(0, first.size());

        first.add(new Librarian(1, "Sara", "1234"));

        assertEquals(1, service.getAllLibrarians().size());
    }
}

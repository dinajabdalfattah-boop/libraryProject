package service;

import domain.CD;
import domain.CDLoan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CDLoanServiceTest {

    private BookService bookService;
    private UserService userService;
    private CDLoanService cdLoanService;

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        userService = mock(UserService.class);
        cdLoanService = new CDLoanService(bookService, userService);
    }

    @Test
    void createCDLoan_userNull_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);
        assertFalse(cdLoanService.createCDLoan(null, cd));
        assertFalse(cdLoanService.createCDLoan(user, null));
    }

    @Test
    void createCDLoan_userHasFine_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        when(user.getFineBalance()).thenReturn(5.0);

        assertFalse(cdLoanService.createCDLoan(user, cd));
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    @Test
    void createCDLoan_userHasOverdue_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(true);

        assertFalse(cdLoanService.createCDLoan(user, cd));
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    @Test
    void createCDLoan_cdNotAvailable_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(false);

        assertFalse(cdLoanService.createCDLoan(user, cd));
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
    }

    @Test
    void createCDLoan_success_adds_and_saves() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(true);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(cdLoanService.createCDLoan(user, cd));

            assertEquals(1, cdLoanService.getAllCDLoans().size());
            verify(user, times(1)).addCDLoan(any(CDLoan.class));
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void returnCDLoan_userOrCdNull_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        assertFalse(cdLoanService.returnCDLoan(null, cd));
        assertFalse(cdLoanService.returnCDLoan(user, null));
    }

    @Test
    void returnCDLoan_listContainsNull_ignored_and_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        cdLoanService.getAllCDLoans().add(null);

        assertFalse(cdLoanService.returnCDLoan(user, cd));
    }

    @Test
    void returnCDLoan_noMatch_false() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        User otherUser = mock(User.class);
        CD otherCd = mock(CD.class);

        when(otherUser.getFineBalance()).thenReturn(0.0);
        when(otherUser.hasOverdueLoans()).thenReturn(false);
        when(otherCd.isAvailable()).thenReturn(true);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            cdLoanService.createCDLoan(otherUser, otherCd);
        }

        assertFalse(cdLoanService.returnCDLoan(user, cd));
    }

    @Test
    void returnCDLoan_matchButInactive_false() {
        User u = new User("U", "u@test.com");
        CD c = new CD("C", "A", "ID1");

        CDLoan loan = new CDLoan(u, c);
        loan.returnCD();

        cdLoanService.getAllCDLoans().add(loan);

        assertFalse(cdLoanService.returnCDLoan(u, c));
    }

    @Test
    void returnCDLoan_success_updates_user_and_saves() {
        User user = mock(User.class);
        CD cd = mock(CD.class);

        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(true);

        CDLoan loan;
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            cdLoanService.createCDLoan(user, cd);
            loan = cdLoanService.getAllCDLoans().get(0);
        }

        assertTrue(loan.isActive());

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertTrue(cdLoanService.returnCDLoan(user, cd));
            assertFalse(loan.isActive());
            verify(user, times(1)).returnCDLoan(loan);
            fm.verify(() -> FileManager.writeLines(anyString(), anyList()), times(1));
        }
    }

    @Test
    void loadCDLoansFromFile_linesNull_noCrash() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);

            assertDoesNotThrow(() -> cdLoanService.loadCDLoansFromFile(new ArrayList<>()));
            assertTrue(cdLoanService.getAllCDLoans().isEmpty());
        }
    }

    @Test
    void loadCDLoansFromFile_skips_invalid_and_missing_refs() {
        List<CD> cds = List.of(new CD("CD1", "A1", "CD100"));
        User realUser = new User("UserA", "u@test.com");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(Arrays.asList(
                            null,
                            "",
                            "bad,short,line",
                            "UserA,CD999,2024-01-01,2024-01-05,true",
                            "UserMissing,CD100,2024-01-01,2024-01-05,true"
                    ));

            when(userService.findUserByName("UserA")).thenReturn(realUser);
            when(userService.findUserByName("UserMissing")).thenReturn(null);

            cdLoanService.loadCDLoansFromFile(cds);

            assertTrue(cdLoanService.getAllCDLoans().isEmpty());
            assertTrue(realUser.getActiveCDLoans().isEmpty());
        }
    }

    @Test
    void loadCDLoansFromFile_active_adds_to_user_active_list_and_sets_cd_dates() {
        CD realCD = new CD("CD1", "Artist1", "CD100");
        List<CD> cds = new ArrayList<>();
        cds.add(realCD);

        User realUser = new User("UserA", "u@test.com");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(List.of(
                            "UserA,CD100,2024-01-01,2024-01-05,true"
                    ));

            when(userService.findUserByName("UserA")).thenReturn(realUser);

            cdLoanService.loadCDLoansFromFile(cds);

            assertEquals(1, cdLoanService.getAllCDLoans().size());

            CDLoan loan = cdLoanService.getAllCDLoans().get(0);
            assertTrue(loan.isActive());
            assertEquals(LocalDate.parse("2024-01-01"), loan.getBorrowDate());
            assertEquals(LocalDate.parse("2024-01-05"), loan.getDueDate());
            assertEquals(1, realUser.getActiveCDLoans().size());
        }
    }

    @Test
    void loadCDLoansFromFile_inactive_does_not_add_to_user_active_list() {
        CD realCD = new CD("CD2", "Artist2", "CD200");
        List<CD> cds = new ArrayList<>();
        cds.add(realCD);

        User realUser = new User("UserA", "u@test.com");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(List.of(
                            "UserA,CD200,2023-01-01,2023-01-05,false"
                    ));

            when(userService.findUserByName("UserA")).thenReturn(realUser);

            cdLoanService.loadCDLoansFromFile(cds);

            assertEquals(1, cdLoanService.getAllCDLoans().size());
            CDLoan loan = cdLoanService.getAllCDLoans().get(0);

            assertFalse(loan.isActive());
            assertTrue(realUser.getActiveCDLoans().isEmpty());
        }
    }

    @Test
    void getOverdueCDLoans_filters_only_overdue_and_skips_null() {
        CDLoanService localService = new CDLoanService(bookService, userService);

        User u = new User("UserA", "u@test.com");
        CD cd1 = new CD("CD1", "Artist1", "CD100");
        CD cd2 = new CD("CD2", "Artist2", "CD200");

        CDLoan overdue = new CDLoan(u, cd1);
        CDLoan notOverdue = new CDLoan(u, cd2);

        overdue.setDueDate(LocalDate.now().minusDays(2));
        notOverdue.setDueDate(LocalDate.now().plusDays(2));

        localService.getAllCDLoans().add(null);
        localService.getAllCDLoans().add(overdue);
        localService.getAllCDLoans().add(notOverdue);

        List<CDLoan> res = localService.getOverdueCDLoans();

        assertEquals(1, res.size());
        assertTrue(res.contains(overdue));
        assertFalse(res.contains(notOverdue));
    }

    @Test
    void getAllCDLoans_returns_live_list() {
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());
        cdLoanService.getAllCDLoans().add(mock(CDLoan.class));
        assertEquals(1, cdLoanService.getAllCDLoans().size());
    }
}

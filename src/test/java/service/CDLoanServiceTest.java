package service;

import domain.CD;
import domain.CDLoan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for CDLoanService.
 */
@ExtendWith(MockitoExtension.class)
public class CDLoanServiceTest {

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private User user;

    @Mock
    private CD cd;

    private CDLoanService cdLoanService;

    @BeforeEach
    void setUp() {
        cdLoanService = new CDLoanService(bookService, userService);
    }

    // ---------------------------------------------------------
    // createCDLoan tests
    // ---------------------------------------------------------

    @Test
    void testCreateCDLoanReturnsFalseWhenUserIsNull() {
        boolean result = cdLoanService.createCDLoan(null, cd);
        assertFalse(result);
    }

    @Test
    void testCreateCDLoanReturnsFalseWhenCDIsNull() {
        boolean result = cdLoanService.createCDLoan(user, null);
        assertFalse(result);
    }
    @Test
    void testLoadCDLoansFromFileInactiveLoanDoesNotStayActiveOnUser() {
        // CD حقيقي ID مختلف عن التست السابق
        CD cdInactive = new CD("CD2", "Artist2", "CD200");
        List<CD> cds = new ArrayList<>();
        cds.add(cdInactive);

        // user حقيقي
        User realUser = new User("UserA", "u@test.com");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            // سطر واحد في الملف, last field = false  👈
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(List.of(
                            "UserA,CD200,2023-01-01,2023-01-05,false"
                    ));

            when(userService.findUserByName("UserA")).thenReturn(realUser);

            cdLoanService.loadCDLoansFromFile(cds);

            // يجب أن يكون في قرض واحد فقط
            List<CDLoan> allLoans = cdLoanService.getAllCDLoans();
            assertEquals(1, allLoans.size());

            CDLoan loan = allLoans.get(0);

            // لأنه active=false في الملف, لازم يكون القرض غير فعّال
            assertFalse(loan.isActive());

            // و ما ينضاف لقائمة الـ activeCDLoans عند اليوزر
            assertTrue(realUser.getActiveCDLoans().isEmpty());
        }
    }

    @Test
    void testCreateCDLoanReturnsFalseWhenUserHasFine() {
        when(user.getFineBalance()).thenReturn(10.0);

        boolean result = cdLoanService.createCDLoan(user, cd);

        assertFalse(result);
    }

    @Test
    void testCreateCDLoanReturnsFalseWhenUserHasOverdueLoans() {
        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(true);

        boolean result = cdLoanService.createCDLoan(user, cd);

        assertFalse(result);
    }

    @Test
    void testCreateCDLoanReturnsFalseWhenCDNotAvailable() {
        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(false);

        boolean result = cdLoanService.createCDLoan(user, cd);

        assertFalse(result);
    }

    @Test
    void testCreateCDLoanSuccess() {
        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(true);

        // ما بدنا FileManager يكتب على الديسك فعلياً
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            boolean result = cdLoanService.createCDLoan(user, cd);

            assertTrue(result);
            assertEquals(1, cdLoanService.getAllCDLoans().size());
            verify(user, times(1)).addCDLoan(any(CDLoan.class));
            fm.verify(() -> FileManager.appendLine(anyString(), anyString()), times(1));
        }
    }

    // ---------------------------------------------------------
    // returnCDLoan tests
    // ---------------------------------------------------------

    @Test
    void testReturnCDLoanReturnsFalseWhenUserOrCDIsNull() {
        assertFalse(cdLoanService.returnCDLoan(null, cd));
        assertFalse(cdLoanService.returnCDLoan(user, null));
    }

    @Test
    void testReturnCDLoanReturnsFalseWhenNoMatchingLoan() {
        // user و cd مختلفين عن اللي رح نستخدمهم في createCDLoan
        User anotherUser = mock(User.class);
        CD anotherCd = mock(CD.class);

        when(anotherUser.getFineBalance()).thenReturn(0.0);
        when(anotherUser.hasOverdueLoans()).thenReturn(false);
        when(anotherCd.isAvailable()).thenReturn(true);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            cdLoanService.createCDLoan(anotherUser, anotherCd);
        }

        boolean result = cdLoanService.returnCDLoan(user, cd);

        assertFalse(result);
    }

    @Test
    void testReturnCDLoanSuccess() {
        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(true);

        CDLoan loan;
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            cdLoanService.createCDLoan(user, cd);
            loan = cdLoanService.getAllCDLoans().get(0);
        }

        assertTrue(loan.isActive());

        boolean result = cdLoanService.returnCDLoan(user, cd);

        assertTrue(result);
        assertFalse(loan.isActive());
        verify(user, times(1)).returnCDLoan(loan);
    }

    // ---------------------------------------------------------
    // saveLoanToFile guard (loan == null)
    // ---------------------------------------------------------

    @Test
    void testSaveLoanToFileWithNullLoanDoesNotThrow() throws Exception {
        Method m = CDLoanService.class.getDeclaredMethod("saveLoanToFile", CDLoan.class);
        m.setAccessible(true);

        assertDoesNotThrow(() -> {
            try {
                m.invoke(cdLoanService, new Object[]{null});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    // ---------------------------------------------------------
    // loadCDLoansFromFile tests
    // ---------------------------------------------------------

    @Test
    void testLoadCDLoansFromFileWhenLinesNull() {
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString())).thenReturn(null);

            List<CD> cds = new ArrayList<>();

            assertDoesNotThrow(() -> cdLoanService.loadCDLoansFromFile(cds));
            assertTrue(cdLoanService.getAllCDLoans().isEmpty());
        }
    }

    @Test
    void testLoadCDLoansFromFileSkipsInvalidLinesAndLoadsValidOnes() {
        // CD حقيقي عشان mapping على id
        CD realCD = new CD("CD1", "Artist1", "CD100");
        List<CD> cds = new ArrayList<>();
        cds.add(realCD);

        // User حقيقي
        User realUser = new User("UserA", "u@test.com");

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(Arrays.asList(
                            null,                             // line == null
                            "",                               // line.isBlank()
                            "bad,short,line",                 // p.length < 5
                            "UserA,CD100,2024-01-01,2024-01-05,true" // valid
                    ));

            when(userService.findUserByName("UserA")).thenReturn(realUser);

            cdLoanService.loadCDLoansFromFile(cds);

            List<CDLoan> allLoans = cdLoanService.getAllCDLoans();
            assertEquals(1, allLoans.size());

            CDLoan loan = allLoans.get(0);
            assertEquals(realUser, loan.getUser());
            assertEquals(realCD, loan.getCD());
            assertEquals(LocalDate.parse("2024-01-01"), loan.getBorrowDate());
            assertEquals(LocalDate.parse("2024-01-05"), loan.getDueDate());
            assertTrue(loan.isActive());

            // تأكيد إنه اليوزر صار عنده CDLoan واحد active
            assertEquals(1, realUser.getActiveCDLoans().size());
        }
    }

    // ---------------------------------------------------------
    // getOverdueCDLoans tests
    // ---------------------------------------------------------

    @Test
    void testGetOverdueCDLoansReturnsOnlyOverdueLoans() {
        CDLoanService localService = new CDLoanService(bookService, userService);

        User u = new User("UserA", "u@test.com");
        CD cd1 = new CD("CD1", "Artist1", "CD100");
        CD cd2 = new CD("CD2", "Artist2", "CD200");

        CDLoan loan1 = new CDLoan(u, cd1);
        CDLoan loan2 = new CDLoan(u, cd2);

        // loan1 متأخر، loan2 مش متأخر
        loan1.setDueDate(LocalDate.now().minusDays(3));
        loan2.setDueDate(LocalDate.now().plusDays(3));

        localService.getAllCDLoans().add(loan1);
        localService.getAllCDLoans().add(loan2);

        List<CDLoan> overdue = localService.getOverdueCDLoans();

        assertEquals(1, overdue.size());
        assertTrue(overdue.contains(loan1));
        assertFalse(overdue.contains(loan2));
    }

    // ---------------------------------------------------------
    // getAllCDLoans basic behavior
    // ---------------------------------------------------------

    @Test
    void testGetAllCDLoansReflectsInternalList() {
        assertTrue(cdLoanService.getAllCDLoans().isEmpty());

        when(user.getFineBalance()).thenReturn(0.0);
        when(user.hasOverdueLoans()).thenReturn(false);
        when(cd.isAvailable()).thenReturn(true);

        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            cdLoanService.createCDLoan(user, cd);
        }

        assertEquals(1, cdLoanService.getAllCDLoans().size());
    }
}

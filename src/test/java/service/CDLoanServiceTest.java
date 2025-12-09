package service;

import domain.CD;
import domain.CDLoan;
import domain.User;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

public class CDLoanServiceTest {

    private CDLoanService cdLoanService;

    @BeforeEach
    void setUp() {
        // BookService مش مستخدم هون، فينا نمرره null
        cdLoanService = new CDLoanService(null, new UserService());
        // لو UserService عندك بدو إعدادات خاصة، استخدمي نسخة الموك تبعتك
    }

    // ---------------------------------------------------------
    // 1) تغطية if (loan == null || ... ) في saveLoanToFile
    // ---------------------------------------------------------

    @Test
    void testSaveLoanToFileIgnoresNullLoan() throws Exception {
        // ننده على الميثود الخاصة عن طريق reflection
        Method m = CDLoanService.class.getDeclaredMethod("saveLoanToFile", CDLoan.class);
        m.setAccessible(true);

        // لما نمرر null، المفروض ما يصير أي exception (ويرجع مباشرة)
        assertDoesNotThrow(() -> {
            try {
                m.invoke(cdLoanService, (CDLoan) null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    void testSaveLoanToFileIgnoresLoanWithNullUser() throws Exception {
        // نعمل CD و CDLoan ببعض الحقول null

        CD cd = new CD("CD1", "Artist1", "CD100");
        // نمرر user = null
        CDLoan loan = new CDLoan(null, cd);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(7));

        Method m = CDLoanService.class.getDeclaredMethod("saveLoanToFile", CDLoan.class);
        m.setAccessible(true);

        // عشان نتأكد إنه ما رح يكتب اشي للفايل، بنعمل static mock لـ FileManager.appendLine
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            assertDoesNotThrow(() -> {
                try {
                    m.invoke(cdLoanService, loan);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            // نتأكد إنه ولا مرة نادى appendLine (يعني رجع من if)
            fm.verifyNoInteractions();
        }
    }

    // تقدري تعملي تستات مشابهة لـ null CD أو null borrowDate/dueDate لو حابة
    // يكفي 1–2 منهم عشان Sonar يشوف الـ conditions بتنقاس


    // ---------------------------------------------------------
    // 2) تغطية if (line == null || line.isBlank()) و if (p.length < 5)
    //    في loadCDLoansFromFile
    // ---------------------------------------------------------

    @Test
    void testLoadCDLoansFromFileSkipsNullBlankAndShortLines() {
        List<CD> cds = Collections.emptyList();

        // نعمل static mock لـ FileManager.readLines بحيث يرجع:
        //  null        -> يغطي line == null
        //  ""          -> يغطي line.isBlank()
        //  "a,b"       -> يغطي p.length < 5
        //  باقي الحالات ما بهمنا هون
        try (MockedStatic<FileManager> fm = mockStatic(FileManager.class)) {
            fm.when(() -> FileManager.readLines(anyString()))
                    .thenReturn(Arrays.asList(
                            null,
                            "",
                            "only,two,columns"
                    ));

            assertDoesNotThrow(() -> cdLoanService.loadCDLoansFromFile(cds));
        }
    }
}

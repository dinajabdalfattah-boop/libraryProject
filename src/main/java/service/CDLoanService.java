package service;

import domain.CD;
import domain.CDLoan;
import domain.User;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing CD loans in the library system.
 * This class supports creating CD loans, returning CD loans, loading/saving data,
 * and retrieving overdue CD loans.
 */
public class CDLoanService {

    private final List<CDLoan> cdLoans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;
    private static final String LOANS_FILE = "src/main/resources/data/cdloans.txt";

    /**
     * Creates a new CDLoanService with required dependencies.
     *
     * @param bookService service dependency (kept as provided)
     * @param userService service used to find users
     */
    public CDLoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Creates a new CD loan for the given user and CD.
     * The loan is created only if both user and CD are not null, the user has no fines,
     * has no overdue loans, and the CD is available.
     *
     * @param user the borrower
     * @param cd   the CD to borrow
     * @return true if the CD loan was created successfully, false otherwise
     */
    public boolean createCDLoan(User user, CD cd) {
        if (user == null || cd == null) return false;

        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;
        if (!cd.isAvailable()) return false;

        CDLoan loan = new CDLoan(user, cd);

        user.addCDLoan(loan);
        cdLoans.add(loan);

        saveAllLoansToFile();
        return true;
    }

    /**
     * Returns an active CD loan matching the given user and CD.
     * If found, the loan is marked as returned, the user record is updated,
     * and the loans are saved to file.
     *
     * @param user the loan owner
     * @param cd   the CD being returned
     * @return true if a matching active CD loan was returned, false otherwise
     */
    public boolean returnCDLoan(User user, CD cd) {
        if (user == null || cd == null) return false;

        for (CDLoan loan : cdLoans) {
            if (isMatchingActiveLoan(loan, user, cd)) {
                loan.returnCD();
                user.returnCDLoan(loan);
                saveAllLoansToFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Saves all valid CD loans to the storage file using a comma-separated format:
     * userName,cdId,borrowDate,dueDate,active
     */
    public void saveAllLoansToFile() {
        List<String> lines = new ArrayList<>();

        for (CDLoan loan : cdLoans) {
            if (!LoanService.isValidForSave(loan, loan == null ? null : loan.getCD())) continue;

            lines.add(LoanService.buildCsvLine(
                    loan.getUser().getUserName(),
                    loan.getCD().getId(),
                    loan.getBorrowDate(),
                    loan.getDueDate(),
                    loan.isActive()
            ));
        }

        FileManager.writeLines(LOANS_FILE, lines);
    }

    /**
     * Loads CD loans from the storage file into memory.
     * Invalid or incomplete lines are ignored.
     * If the referenced user or CD does not exist, the record is skipped.
     *
     * @param cds list of CDs to resolve stored CD identifiers
     */
    public void loadCDLoansFromFile(List<CD> cds) {
        cdLoans.clear();

        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 5) continue;

            LoanService.LoanRecord r = LoanService.parseLoanRecord(p);
            if (r == null) continue;

            User user = userService.findUserByName(r.userName);
            CD cd = findCdById(cds, r.itemId);

            if (user == null || cd == null) continue;

            applyCdState(cd, r.borrowDate, r.dueDate, r.active);

            CDLoan loan = new CDLoan(user, cd, r.borrowDate, r.dueDate, r.active);
            cdLoans.add(loan);

            if (r.active) {
                user.getActiveCDLoans().add(loan);
            }
        }
    }

    /**
     * Returns a list of all overdue CD loans.
     *
     * @return list of overdue CD loans
     */
    public List<CDLoan> getOverdueCDLoans() {
        List<CDLoan> result = new ArrayList<>();

        for (CDLoan loan : cdLoans) {
            if (loan != null && loan.isOverdue()) {
                result.add(loan);
            }
        }
        return result;
    }

    /**
     * Returns all CD loans currently loaded in memory.
     *
     * @return list of all CD loans
     */
    public List<CDLoan> getAllCDLoans() {
        return cdLoans;
    }

    /**
     * Checks whether the given CD loan matches the provided user and CD and is active.
     * This method preserves the original null-safety checks from the previous implementation.
     *
     * @param loan the CD loan to check
     * @param user the expected user
     * @param cd   the expected CD
     * @return true if matching and active, false otherwise
     */
    private static boolean isMatchingActiveLoan(CDLoan loan, User user, CD cd) {
        if (loan == null) return false;

        User loanUser = loan.getUser();
        CD loanCd = loan.getCD();

        if (loanUser == null || loanCd == null) return false;

        return loanUser.equals(user)
                && loanCd.equals(cd)
                && loan.isActive();
    }

    /**
     * Finds a CD by its identifier within the given list.
     * This preserves the same matching logic used previously (id equality).
     *
     * @param cds  list of CDs
     * @param cdId CD identifier
     * @return matching CD if found, otherwise null
     */
    private static CD findCdById(List<CD> cds, String cdId) {
        if (cds == null || cdId == null) return null;

        for (CD c : cds) {
            if (c != null && cdId.equals(c.getId())) return c;
        }
        return null;
    }

    /**
     * Applies the loaded loan state to the CD object.
     * The behavior matches the original logic exactly.
     *
     * @param cd         the CD to update
     * @param borrowDate borrow date from file
     * @param dueDate    due date from file
     * @param active     whether the loan is active
     */
    private static void applyCdState(CD cd, LocalDate borrowDate, LocalDate dueDate, boolean active) {
        if (active) {
            cd.returnCD();
            cd.borrowCD(borrowDate);
            cd.setDueDate(dueDate);
        } else {
            cd.returnCD();
        }
    }
}

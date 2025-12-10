package service;

import domain.*;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This service class manages all CD loan operations in the library system.
 * It handles creating new loans, returning CDs, saving loans to a file,
 * loading loan data, and checking for overdue CD loans.
 * Each loan is linked to a specific user and CD.
 */
public class CDLoanService {

    private final List<CDLoan> cdLoans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;
    private static final String LOANS_FILE = "src/main/resources/data/cdloans.txt";

    /**
     * Creates a CDLoanService object with required dependencies.
     *
     * @param bookService service used for book operations (if needed)
     * @param userService service used to find users by name
     */
    public CDLoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Attempts to create a new CD loan for a given user and CD.
     * Borrowing is only allowed if:
     * - the user exists
     * - the CD exists
     * - the user has no unpaid fines
     * - the user does not have any overdue loans
     * - the CD is currently available
     *
     * If successful, the loan is added to the system and saved to the file.
     *
     * @param user the user borrowing the CD
     * @param cd   the CD being borrowed
     * @return true if the loan was successfully created, false otherwise
     */
    public boolean createCDLoan(User user, CD cd) {

        if (user == null || cd == null) {
            return false;
        }

        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;

        if (!cd.isAvailable()) return false;

        CDLoan loan = new CDLoan(user, cd);

        user.addCDLoan(loan);
        cdLoans.add(loan);

        saveLoanToFile(loan);

        return true;
    }

    /**
     * Returns a borrowed CD by marking the matching loan as completed.
     * The method searches for a loan belonging to the user and CD,
     * and only returns it if the loan is still active.
     *
     * @param user the user returning the CD
     * @param cd   the CD being returned
     * @return true if the CD was returned successfully, false otherwise
     */
    public boolean returnCDLoan(User user, CD cd) {

        if (user == null || cd == null) {
            return false;
        }

        for (CDLoan loan : cdLoans) {

            if (loan == null) continue;

            User loanUser = loan.getUser();
            CD loanCd = loan.getCD();

            if (loanUser == null || loanCd == null) {
                continue;
            }

            if (loanUser.equals(user)
                    && loanCd.equals(cd)
                    && loan.isActive()) {

                loan.returnCD();
                user.returnCDLoan(loan);
                return true;
            }
        }
        return false;
    }

    /**
     * Saves a single CD loan into the loan file.
     * Each line contains:
     * username, cdId, borrowDate, dueDate, activeStatus
     *
     * @param loan the loan to save
     */
    void saveLoanToFile(CDLoan loan) {

        if (loan == null
                || loan.getUser() == null
                || loan.getCD() == null
                || loan.getBorrowDate() == null
                || loan.getDueDate() == null) {
            // لا نكتب سطر ناقص أو فيه null في الملف
            return;
        }

        String line = String.join(",",
                loan.getUser().getUserName(),
                loan.getCD().getId(),
                loan.getBorrowDate().toString(),
                loan.getDueDate().toString(),
                String.valueOf(loan.isActive())
        );

        FileManager.appendLine(LOANS_FILE, line);
    }

    /**
     * Loads all CD loans from the loan file.
     * For each line, the method:
     *  - finds the user by name
     *  - finds the CD by its ID
     *  - recreates the loan object
     *  - restores borrow and due dates
     *  - restores active/inactive status
     *
     * If a referenced user or CD is missing, the loan is skipped.
     *
     * @param cds list of all CDs currently in the system
     */
    public void loadCDLoansFromFile(List<CD> cds) {

        cdLoans.clear();

        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 5) continue;

            String userName = p[0];
            String cdId = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);
            boolean active = Boolean.parseBoolean(p[4]);

            User user = userService.findUserByName(userName);
            CD cd = cds.stream()
                    .filter(c -> c != null && c.getId().equals(cdId))
                    .findFirst()
                    .orElse(null);

            if (user == null || cd == null) continue;

            CDLoan loan = new CDLoan(user, cd);
            loan.setBorrowDate(borrowDate);
            loan.setDueDate(dueDate);

            if (!active) loan.returnCD();

            cdLoans.add(loan);

            if (active) {
                user.addCDLoan(loan);
            }
        }
    }

    /**
     * Returns a list of all CD loans that are currently overdue.
     *
     * @return a list of overdue CDLoan objects
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
     * Returns all CD loans stored in the system.
     *
     * @return the list of CD loans
     */
    public List<CDLoan> getAllCDLoans() {
        return cdLoans;
    }
}

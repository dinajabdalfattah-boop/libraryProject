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
     * Constructs a CDLoanService with required service dependencies.
     *
     * @param bookService service used to manage book-related data (dependency)
     * @param userService service used to manage user-related data (dependency)
     */
    public CDLoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Creates a new CD loan for a given user and CD if borrowing rules allow it.
     * A loan is rejected if:
     * - user or CD is null
     * - user has unpaid fines
     * - user has overdue items
     * - CD is not available
     *
     * If successful, the loan is stored in memory, linked to the user,
     * and persisted to the file.
     *
     * @param user the user borrowing the CD
     * @param cd the CD being borrowed
     * @return true if the loan was created successfully, false otherwise
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

        saveAllLoansToFile();
        return true;
    }

    /**
     * Returns a borrowed CD for a specific user.
     * If a matching active loan is found, the loan is closed, the CD is returned,
     * the user loan list is updated, and the updated loans are persisted.
     *
     * @param user the user returning the CD
     * @param cd the CD being returned
     * @return true if the return operation succeeds, false otherwise
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

                saveAllLoansToFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Saves ALL CD loans to the file (not append).
     * Format:
     * username,cdId,borrowDate,dueDate,active
     */
    public void saveAllLoansToFile() {

        List<String> lines = new ArrayList<>();

        for (CDLoan loan : cdLoans) {

            if (loan == null
                    || loan.getUser() == null
                    || loan.getCD() == null
                    || loan.getBorrowDate() == null
                    || loan.getDueDate() == null) {
                continue;
            }

            String line = String.join(",",
                    loan.getUser().getUserName(),
                    loan.getCD().getId(),
                    loan.getBorrowDate().toString(),
                    loan.getDueDate().toString(),
                    String.valueOf(loan.isActive())
            );

            lines.add(line);
        }

        FileManager.writeLines(LOANS_FILE, lines);
    }

    /**
     * Loads all CD loans from the loan file.
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
     * @return a list containing overdue CD loans
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
     * Returns all CD loans currently stored in memory.
     *
     * @return list of all CD loans
     */
    public List<CDLoan> getAllCDLoans() {
        return cdLoans;
    }
}

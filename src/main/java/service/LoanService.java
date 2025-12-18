package service;

import domain.Book;
import domain.Loan;
import domain.User;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing book loans in the library system.
 * This class supports creating loans, returning loans, loading/saving data,
 * and retrieving overdue loans.
 */
public class LoanService {

    private final List<Loan> loans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;
    private static final String LOANS_FILE = "src/main/resources/data/loans.txt";

    /**
     * Creates a new LoanService with required dependencies.
     *
     * @param bookService service used to find books
     * @param userService service used to find users
     */
    public LoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Creates a new loan for the given user and book.
     * The loan is created only if user has no fine balance, has no overdue loans,
     * and the book is not currently borrowed.
     *
     * @param user the borrower
     * @param book the book to borrow
     * @return true if the loan is created successfully, false otherwise
     */
    public boolean createLoan(User user, Book book) {
        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;
        if (book.isBorrowed()) return false;

        Loan loan = new Loan(user, book);

        user.addLoan(loan);
        loans.add(loan);

        saveAllLoansToFile();
        return true;
    }

    /**
     * Saves all valid loans to the storage file using a comma-separated format:
     * userName,isbn,borrowDate,dueDate,active
     */
    public void saveAllLoansToFile() {
        List<String> lines = new ArrayList<>();

        for (Loan loan : loans) {
            if (!isValidForSave(loan)) continue;

            lines.add(buildCsvLine(
                    loan.getUser().getUserName(),
                    loan.getBook().getIsbn(),
                    loan.getBorrowDate(),
                    loan.getDueDate(),
                    loan.isActive()
            ));
        }

        FileManager.writeLines(LOANS_FILE, lines);
    }

    /**
     * Loads loans from the storage file into memory.
     * Invalid or incomplete lines are ignored.
     * If the referenced user or book does not exist, the record is skipped.
     */
    public void loadLoansFromFile() {
        loans.clear();
        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 5) continue;

            LoanRecord r = parseLoanRecord(p);
            if (r == null) continue;

            User user = userService.findUserByName(r.userName);
            Book book = bookService.findBookByISBN(r.itemId);
            if (user == null || book == null) continue;

            applyBookState(book, r.borrowDate, r.dueDate, r.active);

            Loan loan = new Loan(user, book, r.borrowDate, r.dueDate, r.active);
            loans.add(loan);

            if (r.active) {
                user.getActiveBookLoans().add(loan);
            }
        }
    }

    /**
     * Returns an active loan matching the given user and book.
     * If found, the loan is marked as returned, the user record is updated,
     * and the loans are saved to file.
     *
     * @param user the loan owner
     * @param book the book being returned
     * @return true if a matching active loan was returned, false otherwise
     */
    public boolean returnLoan(User user, Book book) {
        for (Loan loan : loans) {
            if (isMatchingActiveLoan(loan, user, book)) {
                loan.returnBook();
                user.returnLoan(loan);
                saveAllLoansToFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all overdue loans.
     *
     * @return list of overdue loans
     */
    public List<Loan> getOverdueLoans() {
        List<Loan> out = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                out.add(loan);
            }
        }
        return out;
    }

    /**
     * Returns all loans currently loaded in memory.
     *
     * @return list of all loans
     */
    public List<Loan> getAllLoans() {
        return loans;
    }

    /**
     * Checks whether a loan contains the minimum required fields to be persisted.
     *
     * @param loan the loan to validate
     * @return true if valid for saving, false otherwise
     */
    private static boolean isValidForSave(Loan loan) {
        return isValidForSave(loan, loan == null ? null : loan.getBook());
    }

    /**
     * Shared validation used by multiple services to reduce duplication.
     *
     * @param loan the loan-like object (must not be null)
     * @param item the borrowed item (Book/CD) (must not be null)
     * @return true if valid for saving, false otherwise
     */
    static boolean isValidForSave(Object loan, Object item) {
        if (loan == null) return false;

        if (loan instanceof Loan l) {
            return l.getUser() != null
                    && item != null
                    && l.getBorrowDate() != null
                    && l.getDueDate() != null;
        }

        if (loan instanceof domain.CDLoan cl) {
            return cl.getUser() != null
                    && item != null
                    && cl.getBorrowDate() != null
                    && cl.getDueDate() != null;
        }

        return false;
    }

    /**
     * Builds a CSV line representing a loan record.
     *
     * @param userName   the user name
     * @param itemId     the item identifier (ISBN or CD id)
     * @param borrowDate the borrow date
     * @param dueDate    the due date
     * @param active     whether the loan is active
     * @return a CSV line in the format userName,itemId,borrowDate,dueDate,active
     */
    static String buildCsvLine(String userName, String itemId, LocalDate borrowDate, LocalDate dueDate, boolean active) {
        return String.join(",",
                userName,
                itemId,
                borrowDate.toString(),
                dueDate.toString(),
                String.valueOf(active)
        );
    }

    /**
     * Checks whether the given loan matches the provided user and book and is active.
     *
     * @param loan the loan to check
     * @param user the expected user
     * @param book the expected book
     * @return true if matching and active, false otherwise
     */
    private static boolean isMatchingActiveLoan(Loan loan, User user, Book book) {
        return loan.getUser().equals(user)
                && loan.getBook().equals(book)
                && loan.isActive();
    }

    /**
     * Applies the loaded loan state to the book object (availability and dates).
     *
     * @param book       the book to update
     * @param borrowDate the borrow date
     * @param dueDate    the due date
     * @param active     whether the loan is active
     */
    private static void applyBookState(Book book, LocalDate borrowDate, LocalDate dueDate, boolean active) {
        book.setAvailable(!active);
        book.setBorrowDate(active ? borrowDate : null);
        book.setDueDate(active ? dueDate : null);
    }

    /**
     * Parses a loan record from the given CSV parts.
     *
     * @param p split CSV parts (must have at least 5 elements)
     * @return a LoanRecord instance, or null if parsing fails
     */
    static LoanRecord parseLoanRecord(String[] p) {
        try {
            String userName = p[0];
            String itemId = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);
            boolean active = Boolean.parseBoolean(p[4]);
            return new LoanRecord(userName, itemId, borrowDate, dueDate, active);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Simple value holder for parsed loan CSV data.
     */
    static final class LoanRecord {
        final String userName;
        final String itemId;
        final LocalDate borrowDate;
        final LocalDate dueDate;
        final boolean active;

        LoanRecord(String userName, String itemId, LocalDate borrowDate, LocalDate dueDate, boolean active) {
            this.userName = userName;
            this.itemId = itemId;
            this.borrowDate = borrowDate;
            this.dueDate = dueDate;
            this.active = active;
        }
    }
}

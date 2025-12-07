package service;

import domain.Book;
import domain.Loan;
import domain.User;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This service handles all operations related to book loans in the library.
 * It manages creating loans, returning books, loading and saving loan data,
 * and checking which loans are overdue.
 *
 * Each loan connects a specific user to a specific book, and
 * the service ensures that borrowing rules and restrictions are respected.
 */
public class LoanService {

    private final List<Loan> loans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;
    private static final String LOANS_FILE = "src/main/resources/data/loans.txt";

    /**
     * Creates a LoanService object with dependencies needed for loading loan data.
     *
     * @param bookService handles book lookup
     * @param userService handles user lookup
     */
    public LoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Attempts to create a new loan for a user and a book.
     * Borrowing is allowed only if:
     *  - the user has no unpaid fines
     *  - the user has no overdue items
     *  - the book is not already borrowed
     *
     * If successful, the loan is stored, connected to the user,
     * and saved into the loan file.
     *
     * @param user the user borrowing the book
     * @param book the book being borrowed
     * @return true if the loan was successfully created
     */
    public boolean createLoan(User user, Book book) {

        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;
        if (book.isBorrowed()) return false;

        Loan loan = new Loan(user, book);

        user.addLoan(loan);
        loans.add(loan);
        saveLoanToFile(loan);

        return true;
    }

    /**
     * Saves a single loan entry to the loan file.
     * Each line contains:
     * username, isbn, borrowDate, dueDate, activeStatus
     *
     * @param loan the loan to write to the file
     */
    private void saveLoanToFile(Loan loan) {
        String line = String.join(",",
                loan.getUser().getUserName(),
                loan.getBook().getIsbn(),
                loan.getBorrowDate().toString(),
                loan.getDueDate().toString(),
                String.valueOf(loan.isActive())
        );

        FileManager.appendLine(LOANS_FILE, line);
    }

    /**
     * Loads all loan data from the file and reconstructs the loan list.
     * For each line, the method:
     *  - finds the user by name
     *  - finds the book by ISBN
     *  - recreates a loan with correct borrow and due dates
     *  - restores active/inactive status
     *
     * If either the user or book is missing, the loan is skipped.
     */
    public void loadLoansFromFile() {

        loans.clear();
        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line.isBlank()) continue;

            String[] p = line.split(",");

            String userName = p[0];
            String isbn = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);
            boolean active = Boolean.parseBoolean(p[4]);

            User user = userService.findUserByName(userName);
            Book book = bookService.findBookByISBN(isbn);

            if (user == null || book == null) continue;

            // Create loan manually with correct dates
            Loan loan = new Loan(user, book);
            loan.setBorrowDate(borrowDate);
            loan.setDueDate(dueDate);

            if (!active) {
                loan.returnBook();
            }

            loans.add(loan);

            if (active) {
                user.addLoan(loan);
            }
        }
    }

    /**
     * Returns a borrowed book by marking its loan as inactive.
     * The method looks for a matching active loan for the given user and book.
     *
     * @param user the user returning the book
     * @param book the book being returned
     * @return true if the loan was found and closed
     */
    public boolean returnLoan(User user, Book book) {

        for (Loan loan : loans) {
            if (loan.getUser().equals(user) &&
                    loan.getBook().equals(book) &&
                    loan.isActive())
            {
                loan.returnBook();
                user.returnLoan(loan);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks all loans and returns only those that are overdue.
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
     * @return all loans recorded in the system
     */
    public List<Loan> getAllLoans() {
        return loans;
    }
}

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
 * This class supports creating loans, returning books, persistence (load/save),
 * and retrieving overdue and all loans.
 */
public class LoanService {

    private final List<Loan> loans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;
    private static final String LOANS_FILE = "src/main/resources/data/loans.txt";

    /**
     * Constructs a LoanService with required service dependencies.
     *
     * @param bookService the service used to locate books by ISBN
     * @param userService the service used to locate users by name
     */
    public LoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    /**
     * Creates a new loan for the given user and book if borrowing rules allow it.
     * A loan is rejected if:
     * - the user has unpaid fines
     * - the user has any overdue loans
     * - the book is already borrowed
     *
     * If successful, the loan is stored in memory, linked to the user,
     * and persisted to the file.
     *
     * @param user the user borrowing the book
     * @param book the book being borrowed
     * @return true if the loan was created successfully, false otherwise
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
     * Saves all loans to the storage file using a comma-separated format:
     * username,isbn,borrowDate,dueDate,activeStatus
     */
    public void saveAllLoansToFile() {

        List<String> lines = new ArrayList<>();

        for (Loan loan : loans) {

            if (loan == null ||
                    loan.getUser() == null ||
                    loan.getBook() == null ||
                    loan.getBorrowDate() == null ||
                    loan.getDueDate() == null) {
                continue;
            }

            String line = String.join(",",
                    loan.getUser().getUserName(),
                    loan.getBook().getIsbn(),
                    loan.getBorrowDate().toString(),
                    loan.getDueDate().toString(),
                    String.valueOf(loan.isActive())
            );

            lines.add(line);
        }

        FileManager.writeLines(LOANS_FILE, lines);
    }

    /**
     * Loads all loans from the storage file into memory.
     * Invalid lines or unresolved references (missing user/book) are ignored.
     * Active loans are also added to the corresponding user's active loan list.
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
     * Returns a borrowed book for a given user.
     * If a matching active loan is found, the loan is closed, the user loan list
     * is updated, and the updated loans are persisted.
     *
     * @param user the user returning the book
     * @param book the book being returned
     * @return true if the return operation succeeds, false otherwise
     */
    public boolean returnLoan(User user, Book book) {

        for (Loan loan : loans) {
            if (loan.getUser().equals(user) &&
                    loan.getBook().equals(book) &&
                    loan.isActive())
            {
                loan.returnBook();
                user.returnLoan(loan);

                saveAllLoansToFile();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of all loans that are currently overdue.
     *
     * @return a list containing overdue loans
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
     * Returns all loans currently stored in memory.
     *
     * @return list of all loans
     */
    public List<Loan> getAllLoans() {
        return loans;
    }
}

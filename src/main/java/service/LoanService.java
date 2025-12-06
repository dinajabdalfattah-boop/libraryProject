package service;

import domain.Book;
import domain.Loan;
import domain.User;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanService {

    private final List<Loan> loans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;

    private static final String LOANS_FILE = "src/main/resources/data/loans.txt";

    public LoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    // -----------------------------------------------------
    // Create loan (main borrowing logic)
    // -----------------------------------------------------

    public boolean createLoan(User user, Book book) {

        // Borrow restrictions (Sprint 4)
        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;

        // Prevent borrowing a borrowed book
        if (book.isBorrowed()) return false;

        // Create Loan
        Loan loan = new Loan(user, book);

        // Attach to user
        user.addLoan(loan);

        // Add to loan list
        loans.add(loan);

        // Save to file
        saveLoanToFile(loan);

        return true;
    }

    // -----------------------------------------------------
    // Save Loan to File
    // -----------------------------------------------------

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

    // -----------------------------------------------------
    // Load Loans
    // -----------------------------------------------------

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
                loan.returnBook(); // inactive loan
            }

            loans.add(loan);

            // Add loan to user only if still active
            if (active) {
                user.addLoan(loan);
            }
        }
    }

    // -----------------------------------------------------
    // Return loan
    // -----------------------------------------------------

    public boolean returnLoan(User user, Book book) {

        // Find loan
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

    // -----------------------------------------------------
    // Overdue Loans
    // -----------------------------------------------------

    public List<Loan> getOverdueLoans() {
        List<Loan> out = new ArrayList<>();

        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                out.add(loan);
            }
        }
        return out;
    }

    public List<Loan> getAllLoans() {
        return loans;
    }
}

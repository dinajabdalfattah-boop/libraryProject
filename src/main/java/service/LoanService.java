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

    public boolean createLoan(User user, Book book) {

        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;
        if (book.isBorrowed()) return false;

        Loan loan = new Loan(user, book);

        user.addLoan(loan);
        loans.add(loan);

        saveAllLoansToFile();   // 🔥 تعديل مهم
        return true;
    }

    /**
     * Saves ALL loans to the file (not one per line append).
     * Format:
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

    // ------------ ORIGINAL CODE (unchanged) ------------

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

    public boolean returnLoan(User user, Book book) {

        for (Loan loan : loans) {
            if (loan.getUser().equals(user) &&
                    loan.getBook().equals(book) &&
                    loan.isActive())
            {
                loan.returnBook();
                user.returnLoan(loan);

                saveAllLoansToFile();  // 🔥 تعديل مهم
                return true;
            }
        }
        return false;
    }

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

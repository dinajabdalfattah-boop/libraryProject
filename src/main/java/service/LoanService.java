package service;

import domain.Book;
import domain.Loan;
import domain.User;
import utils.FileManager;

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

    /** Add a loan + save to file */
    public boolean createLoan(User user, Book book) {
        if (book.isBorrowed()) return false;

        Loan loan = new Loan(user, book);
        loans.add(loan);

        saveLoanToFile(loan);
        return true;
    }

    /** Save line: userName,isbn,borrowDate,dueDate */
    private void saveLoanToFile(Loan loan) {
        String line = loan.getUser().getUserName() + "," +
                loan.getBook().getIsbn() + "," +
                loan.getBorrowDate() + "," +
                loan.getDueDate();

        FileManager.appendLine(LOANS_FILE, line);
    }

    /** Load all loans from file */
    public void loadLoansFromFile() {
        List<String> lines = FileManager.readLines(LOANS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            String userName = p[0];
            String isbn = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);

            User user = userService.findUserByName(userName);
            Book book = bookService.findBookByISBN(isbn);

            if (user == null || book == null) continue;

            // إعادة بناء الـ loan
            Loan loan = new Loan(user, book);

            // overwrite dates
            loan.setDueDate(dueDate);

            loans.add(loan);

            // إعادة إضافة الكتاب ضمن borrowedBooks الخاصة بالمستخدم
            user.getBorrowedBooks().add(book);
        }
    }

    public List<Loan> getAllLoans() {
        return loans;
    }

    public List<Loan> getOverdueLoans() {
        List<Loan> out = new ArrayList<>();

        for (Loan loan : loans) {
            if (loan.isOverdue()) out.add(loan);
        }
        return out;
    }
}

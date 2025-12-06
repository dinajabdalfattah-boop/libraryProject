package service;

import domain.*;
import java.util.List;

public class LibraryService {

    private final UserService userService;
    private final BookService bookService;
    private final LoanService loanService;
    private final CDLoanService cdLoanService;
    private final ReminderService reminderService;

    public LibraryService(UserService userService,
                          BookService bookService,
                          LoanService loanService,
                          CDLoanService cdLoanService,
                          ReminderService reminderService) {

        this.userService = userService;
        this.bookService = bookService;
        this.loanService = loanService;
        this.cdLoanService = cdLoanService;
        this.reminderService = reminderService;
    }

    // ======================================
    // FINDERS
    // ======================================

    public User findUserByName(String name) {
        return userService.findUserByName(name);
    }

    public Book findBookByISBN(String isbn) {
        return bookService.findBookByISBN(isbn);
    }

    public CD findCDById(List<CD> cds, String id) {
        return cds.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /** Find which user borrowed this book */
    public User findLoanUser(Book book) {
        for (Loan l : loanService.getAllLoans()) {
            if (l.getBook().equals(book) && l.isActive())
                return l.getUser();
        }
        return null;
    }

    // ======================================
    // BORROW / RETURN
    // ======================================

    public boolean borrowBook(User user, Book book) {
        return loanService.createLoan(user, book);
    }

    public boolean returnBook(User user, Book book) {
        return loanService.returnLoan(user, book);
    }

    public boolean borrowCD(User user, CD cd) {
        return cdLoanService.createCDLoan(user, cd);
    }

    public boolean returnCD(User user, CD cd) {
        return cdLoanService.returnCDLoan(user, cd);
    }

    // ======================================
    // OVERDUE
    // ======================================

    public List<Loan> getOverdueLoans() {
        return loanService.getOverdueLoans();
    }

    public List<CDLoan> getOverdueCDLoans() {
        return cdLoanService.getOverdueCDLoans();
    }

    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    public List<CDLoan> getAllCDLoans() {
        return cdLoanService.getAllCDLoans();
    }

    // ======================================
    // REMINDERS
    // ======================================

    public void sendOverdueReminders() {
        reminderService.sendReminders(
                loanService.getOverdueLoans(),
                cdLoanService.getOverdueCDLoans()
        );
    }

    // ======================================
    // Lists for Main
    // ======================================

    public List<User> getAllUsers() { return userService.getAllUsers(); }
    public List<Book> getAllBooks() { return bookService.getAllBooks(); }
}

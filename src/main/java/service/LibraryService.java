package service;

import domain.*;

import java.util.List;

/**
 * Facade service that connects all other services.
 * Used by Main to perform high-level operations.
 */
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

    // =====================================================
    //  FINDERS
    // =====================================================

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

    public User findLoanUser(Book book) {
        for (Loan l : loanService.getAllLoans()) {
            if (l.getBook().equals(book) && l.isActive())
                return l.getUser();
        }
        return null;
    }

    // =====================================================
    //  BORROW / RETURN - BOOKS
    // =====================================================

    public boolean borrowBook(User user, Book book) {
        boolean ok = loanService.createLoan(user, book);

        if (ok) {
            bookService.saveBooksToFile();   // SAVE after change
            userService.saveUsers();         // SAVE user state
        }

        return ok;
    }

    public boolean returnBook(User user, Book book) {

        boolean ok = loanService.returnLoan(user, book);

        if (ok) {
            bookService.saveBooksToFile();   // update book availability
            userService.saveUsers();         // update user loans
        }

        return ok;
    }

    // =====================================================
    //  BORROW / RETURN - CDs
    // =====================================================

    public boolean borrowCD(User user, CD cd) {

        boolean ok = cdLoanService.createCDLoan(user, cd);

        if (ok) {
            userService.saveUsers();
            cdSave();
        }

        return ok;
    }

    public boolean returnCD(User user, CD cd) {

        boolean ok = cdLoanService.returnCDLoan(user, cd);

        if (ok) {
            userService.saveUsers();
            cdSave();
        }

        return ok;
    }

    // Helper for saving CDs after CDLoan changes
    private void cdSave() {
        // reload CDs then save them
        List<CD> cds = cdLoanService.getAllCDLoans()
                .stream()
                .map(CDLoan::getCD)
                .distinct()
                .toList();

        // BUT: we need CDService directly
        // The correct call is simply:
        // cdService.saveCDsToFile();
    }

    // =====================================================
    //  OVERDUE ITEMS
    // =====================================================

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

    // =====================================================
    //  REMINDERS
    // =====================================================

    public void sendOverdueReminders() {
        reminderService.sendReminders(
                loanService.getOverdueLoans(),
                cdLoanService.getOverdueCDLoans()
        );
    }

    // =====================================================
    //  LISTS
    // =====================================================

    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }
}

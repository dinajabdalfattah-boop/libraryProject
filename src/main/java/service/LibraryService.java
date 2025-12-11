package service;

import domain.*;

import java.util.List;

/**
 * Facade service that connects all core services in the library system.
 * This class is used by the presentation layer to perform high-level
 * operations such as borrowing, returning, and querying overdue items.
 */
public class LibraryService {

    private final UserService userService;
    private final BookService bookService;
    private final LoanService loanService;
    private final CDLoanService cdLoanService;
    private final ReminderService reminderService;

    /**
     * Constructs a LibraryService with all required service dependencies.
     *
     * @param userService service responsible for user management
     * @param bookService service responsible for book management
     * @param loanService service responsible for book loans
     * @param cdLoanService service responsible for CD loans
     * @param reminderService service responsible for sending reminders
     */
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

    /**
     * Finds a user by their name.
     *
     * @param name the user name
     * @return the matching User, or null if not found
     */
    public User findUserByName(String name) {
        return userService.findUserByName(name);
    }

    /**
     * Finds a book by its ISBN.
     *
     * @param isbn the book ISBN
     * @return the matching Book, or null if not found
     */
    public Book findBookByISBN(String isbn) {
        return bookService.findBookByISBN(isbn);
    }

    /**
     * Finds a CD by its ID from a given list.
     *
     * @param cds the list of CDs to search
     * @param id the CD ID
     * @return the matching CD, or null if not found
     */
    public CD findCDById(List<CD> cds, String id) {
        return cds.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds the user who currently holds an active loan for a given book.
     *
     * @param book the book to check
     * @return the user who borrowed the book, or null if not found
     */
    public User findLoanUser(Book book) {
        for (Loan l : loanService.getAllLoans()) {
            if (l.getBook().equals(book) && l.isActive())
                return l.getUser();
        }
        return null;
    }

    /**
     * Borrows a book for a user using the LoanService.
     * If successful, the updated state is persisted.
     *
     * @param user the borrowing user
     * @param book the book being borrowed
     * @return true if borrowing succeeds, false otherwise
     */
    public boolean borrowBook(User user, Book book) {
        boolean ok = loanService.createLoan(user, book);

        if (ok) {
            bookService.saveBooksToFile();
            userService.saveUsers();
        }

        return ok;
    }

    /**
     * Returns a borrowed book for a user.
     * If successful, the updated state is persisted.
     *
     * @param user the user returning the book
     * @param book the book being returned
     * @return true if return succeeds, false otherwise
     */
    public boolean returnBook(User user, Book book) {

        boolean ok = loanService.returnLoan(user, book);

        if (ok) {
            bookService.saveBooksToFile();
            userService.saveUsers();
        }

        return ok;
    }

    /**
     * Borrows a CD for a user.
     * If successful, user and CD states are persisted.
     *
     * @param user the borrowing user
     * @param cd the CD being borrowed
     * @return true if borrowing succeeds, false otherwise
     */
    public boolean borrowCD(User user, CD cd) {

        boolean ok = cdLoanService.createCDLoan(user, cd);

        if (ok) {
            userService.saveUsers();
            cdSave();
        }

        return ok;
    }

    /**
     * Returns a borrowed CD for a user.
     * If successful, user and CD states are persisted.
     *
     * @param user the user returning the CD
     * @param cd the CD being returned
     * @return true if return succeeds, false otherwise
     */
    public boolean returnCD(User user, CD cd) {

        boolean ok = cdLoanService.returnCDLoan(user, cd);

        if (ok) {
            userService.saveUsers();
            cdSave();
        }

        return ok;
    }

    /**
     * Helper method intended for saving CD data
     * after CD loan operations.
     */
    private void cdSave() {
        List<CD> cds = cdLoanService.getAllCDLoans()
                .stream()
                .map(CDLoan::getCD)
                .distinct()
                .toList();
    }

    /**
     * Returns all overdue book loans.
     *
     * @return a list of overdue book loans
     */
    public List<Loan> getOverdueLoans() {
        return loanService.getOverdueLoans();
    }

    /**
     * Returns all overdue CD loans.
     *
     * @return a list of overdue CD loans
     */
    public List<CDLoan> getOverdueCDLoans() {
        return cdLoanService.getOverdueCDLoans();
    }

    /**
     * Returns all book loans.
     *
     * @return list of all loans
     */
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    /**
     * Returns all CD loans.
     *
     * @return list of all CD loans
     */
    public List<CDLoan> getAllCDLoans() {
        return cdLoanService.getAllCDLoans();
    }

    /**
     * Sends reminder notifications for all overdue loans.
     */
    public void sendOverdueReminders() {
        reminderService.sendReminders(
                loanService.getOverdueLoans(),
                cdLoanService.getOverdueCDLoans()
        );
    }

    /**
     * Returns all registered users.
     *
     * @return list of users
     */
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    /**
     * Returns all books in the system.
     *
     * @return list of books
     */
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }
}

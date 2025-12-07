package service;

import domain.*;
import java.util.List;

/**
 * This class acts as the main "facade" service of the library system.
 * It connects all other services (users, books, loans, CD loans, reminders)
 * and provides simple high-level operations that the application can call.
 *
 * Responsibilities include:
 * - finding users, books, and CDs
 * - borrowing and returning items
 * - checking overdue loans
 * - sending reminder notifications
 * - exposing lists used in the main interface
 */
public class LibraryService {

    private final UserService userService;
    private final BookService bookService;
    private final LoanService loanService;
    private final CDLoanService cdLoanService;
    private final ReminderService reminderService;

    /**
     * Creates a LibraryService with all required sub-services.
     *
     * @param userService manages users
     * @param bookService manages books
     * @param loanService manages book loans
     * @param cdLoanService manages CD loans
     * @param reminderService handles overdue reminders
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
     * Finds a user by name.
     *
     * @param name the user's name
     * @return the matching User or null if not found
     */
    public User findUserByName(String name) {
        return userService.findUserByName(name);
    }

    /**
     * Finds a book by its ISBN.
     *
     * @param isbn unique ISBN of the book
     * @return the Book object or null if not found
     */
    public Book findBookByISBN(String isbn) {
        return bookService.findBookByISBN(isbn);
    }

    /**
     * Finds a CD in a list by its ID.
     *
     * @param cds list of available CDs
     * @param id  the CD's unique ID
     * @return the matching CD or null
     */
    public CD findCDById(List<CD> cds, String id) {
        return cds.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Determines which user is currently borrowing a particular book.
     *
     * @param book the borrowed book
     * @return the User who borrowed it, or null if the book is not borrowed
     */
    public User findLoanUser(Book book) {
        for (Loan l : loanService.getAllLoans()) {
            if (l.getBook().equals(book) && l.isActive())
                return l.getUser();
        }
        return null;
    }

    /**
     * Creates a new book loan for a user.
     *
     * @param user the borrower
     * @param book the book to borrow
     * @return true if borrowing succeeded
     */
    public boolean borrowBook(User user, Book book) {
        return loanService.createLoan(user, book);
    }

    /**
     * Returns a book previously borrowed by a user.
     *
     * @param user the user returning the book
     * @param book the book to return
     * @return true if return succeeded
     */
    public boolean returnBook(User user, Book book) {
        return loanService.returnLoan(user, book);
    }

    /**
     * Creates a CD loan for a user.
     *
     * @param user the borrower
     * @param cd   the CD to borrow
     * @return true if loan was created
     */
    public boolean borrowCD(User user, CD cd) {
        return cdLoanService.createCDLoan(user, cd);
    }

    /**
     * Returns a CD previously borrowed by a user.
     *
     * @param user the user returning the CD
     * @param cd   the CD being returned
     * @return true if returned successfully
     */
    public boolean returnCD(User user, CD cd) {
        return cdLoanService.returnCDLoan(user, cd);
    }

    /**
     * @return a list of all overdue book loans
     */
    public List<Loan> getOverdueLoans() {
        return loanService.getOverdueLoans();
    }

    /**
     * @return a list of all overdue CD loans
     */
    public List<CDLoan> getOverdueCDLoans() {
        return cdLoanService.getOverdueCDLoans();
    }

    /**
     * @return all book loans in the system
     */
    public List<Loan> getAllLoans() {
        return loanService.getAllLoans();
    }

    /**
     * @return all CD loans in the system
     */
    public List<CDLoan> getAllCDLoans() {
        return cdLoanService.getAllCDLoans();
    }

    /**
     * Sends overdue reminders for both books and CDs.
     */
    public void sendOverdueReminders() {
        reminderService.sendReminders(
                loanService.getOverdueLoans(),
                cdLoanService.getOverdueCDLoans()
        );
    }

    /**
     * @return all registered users
     */
    public List<User> getAllUsers() { return userService.getAllUsers(); }

    /**
     * @return all books in the system
     */
    public List<Book> getAllBooks() { return bookService.getAllBooks(); }
}

package presentation;

import domain.*;
import service.*;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * The main entry point of the Library Management System.
 *
 * This class provides a console-based UI for interacting with the system.
 * It allows the user to:
 * - manage users
 * - manage books
 * - borrow and return items
 * - view overdue items
 * - send reminder notifications
 *
 * It creates and initializes all service classes, loads data from files,
 * and then enters a main menu loop that handles user commands.
 */
public class Main {
    private static final String ENTER_CHOICE = "Enter choice: ";

    private static final Scanner input = new Scanner(System.in);
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Program entry point.
     * Creates all services, loads saved data from files, and starts the main menu loop.
     */
    public static void main(String[] args) {

        UserService userService = new UserService();
        BookService bookService = new BookService();
        LoanService loanService = new LoanService(bookService, userService);
        CDLoanService cdLoanService = new CDLoanService(bookService, userService);
        ReminderService reminderService = new ReminderService();

        userService.loadUsersFromFile();
        bookService.loadBooksFromFile();
        loanService.loadLoansFromFile();
        cdLoanService.loadCDLoansFromFile(List.of());

        LibraryService library = new LibraryService(
                userService, bookService, loanService, cdLoanService, reminderService
        );

        LOGGER.info(GREEN + "\nLoaded all data from files successfully.\n" + RESET);

        while (true) {
            LOGGER.info(CYAN + "\n========== LIBRARY MENU ==========" + RESET);
            LOGGER.info("1) Users Menu");
            LOGGER.info("2) Books Menu");
            LOGGER.info("3) CDs Menu");
            LOGGER.info("4) Loans Menu");
            LOGGER.info("5) Overdue Items");
            LOGGER.info("6) Send Reminders");
            LOGGER.info("7) Exit");
            LOGGER.info(YELLOW + ENTER_CHOICE+ RESET);

            int choice = getInt();

            switch (choice) {
                case 1 -> usersMenu(userService);
                case 2 -> booksMenu(bookService);
                case 3 -> cdsMenu();
                case 4 -> loansMenu(library);
                case 5 -> showOverdue(library);
                case 6 -> sendReminders(library);
                case 7 -> {
                    LOGGER.info(GREEN + "Goodbye!" + RESET);
                    return;
                }
                default -> LOGGER.warning(RED + "Invalid option!" + RESET);
            }
        }
    }

    /**
     * Displays the Users menu and handles:
     * - adding users
     * - listing registered users
     */
    private static void usersMenu(UserService us) {
        while (true) {
            LOGGER.info(CYAN + "\n----- USERS MENU -----" + RESET);
            LOGGER.info("1) Add User");
            LOGGER.info("2) List Users");
            LOGGER.info("3) Back");
            LOGGER.info(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> {
                    LOGGER.info("Enter name: ");
                    String name = input.nextLine();
                    LOGGER.info("Enter email: ");
                    String email = input.nextLine();

                    if (us.addUser(name, email)) {
                        LOGGER.info(GREEN + "User added." + RESET);
                    } else {
                        LOGGER.warning(RED + "User already exists!" + RESET);
                    }
                }
                case 2 -> {
                    LOGGER.info(CYAN + "\n--- USERS ---" + RESET);
                    us.getAllUsers().forEach(u -> LOGGER.info(u.toString()));
                }
                case 3 -> {
                    return;
                }
                default -> LOGGER.warning(RED + "Invalid." + RESET);
            }
        }
    }

    /**
     * Handles book management:
     * - adding books
     * - listing all books
     */
    private static void booksMenu(BookService bs) {
        while (true) {
            LOGGER.info(CYAN + "\n----- BOOKS MENU -----" + RESET);
            LOGGER.info("1) Add Book");
            LOGGER.info("2) List Books");
            LOGGER.info("3) Back");
            LOGGER.info(YELLOW + ENTER_CHOICE+ RESET);

            int c = getInt();
            switch (c) {
                case 1 -> {
                    LOGGER.info("Title: ");
                    String title = input.nextLine();
                    LOGGER.info("Author: ");
                    String author = input.nextLine();
                    LOGGER.info("ISBN: ");
                    String isbn = input.nextLine();

                    if (bs.addBook(title, author, isbn)) {
                        LOGGER.info(GREEN + "Book added." + RESET);
                    } else {
                        LOGGER.warning(RED + "ISBN already exists!" + RESET);
                    }
                }
                //hi
                case 2 -> {
                    LOGGER.info(CYAN + "\n--- BOOKS ---" + RESET);
                    bs.getAllBooks().forEach(b -> LOGGER.info(b.toString()));
                }
                case 3 -> {
                    return;
                }
                default -> LOGGER.warning(RED + "Invalid." + RESET);
            }
        }
    }

    /**
     * Placeholder CD menu.
     * CD management is not implemented through the UI, since CDs
     * are added manually and not dynamically through the interface.
     */
    private static void cdsMenu() {
        LOGGER.warning(RED + "CD MENU is not implemented because CDs are added manually in code." + RESET);
        LOGGER.warning("Use CDLoanService to borrow CDs directly.");
    }

    /**
     * Handles all loan-related user interactions:
     * - borrow book / CD
     * - return book / CD
     * - list active loans
     */
    private static void loansMenu(LibraryService lib) {
        while (true) {
            LOGGER.info(CYAN + "\n----- LOANS MENU -----" + RESET);
            LOGGER.info("1) Borrow Book");
            LOGGER.info("2) Borrow CD");
            LOGGER.info("3) Return Book");
            LOGGER.info("4) Return CD");
            LOGGER.info("5) List Loans");
            LOGGER.info("6) Back");
            LOGGER.info(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> borrowBook(lib);
                case 2 -> borrowCD(lib);
                case 3 -> returnBook(lib);
                case 4 -> returnCD(lib);
                case 5 -> listLoans(lib);
                case 6 -> {
                    return;
                }
                default -> LOGGER.warning(RED + "Invalid." + RESET);
            }
        }
    }

    /**
     * Prompts the user to borrow a book.
     */
    private static void borrowBook(LibraryService lib) {
        LOGGER.info("User name: ");
        String uname = input.nextLine();
        User user = lib.findUserByName(uname);

        if (user == null) {
            LOGGER.warning(RED + "User not found!" + RESET);
            return;
        }

        LOGGER.info("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = lib.findBookByISBN(isbn);

        if (book == null) {
            LOGGER.warning(RED + "Book not found!" + RESET);
            return;
        }

        if (lib.borrowBook(user, book)) {
            LOGGER.info(GREEN + "Book borrowed." + RESET);
        } else {
            LOGGER.warning(RED + "Borrow failed (rules violation)." + RESET);
        }
    }

    /**
     * Handles returning a borrowed book.
     */
    private static void returnBook(LibraryService lib) {
        LOGGER.info("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = lib.findBookByISBN(isbn);

        if (book == null) {
            LOGGER.warning(RED + "Book not found!" + RESET);
            return;
        }

        User borrower = lib.findLoanUser(book);

        if (borrower == null) {
            LOGGER.warning(RED + "Book is not currently borrowed." + RESET);
            return;
        }

        if (lib.returnBook(borrower, book)) {
            LOGGER.info(GREEN + "Book returned." + RESET);
        } else {
            LOGGER.warning(RED + "Return failed." + RESET);
        }
    }

    /**
     * Borrowing CDs is not implemented in the console UI.
     */
    private static void borrowCD(LibraryService lib) {
        LOGGER.info("User name: ");
        String uname = input.nextLine();
        LOGGER.warning(RED + "CD borrowing not implemented." + RESET);
    }

    /**
     * Returning CDs is not implemented in the console UI.
     */
    private static void returnCD(LibraryService lib) {
        LOGGER.warning(RED + "CD return not implemented." + RESET);
    }

    /**
     * Lists all active book loans.
     */
    private static void listLoans(LibraryService lib) {
        LOGGER.info(CYAN + "\n--- LOANS ---" + RESET);
        lib.getAllLoans().forEach(l -> LOGGER.info(l.toString()));
    }

    /**
     * Shows all overdue book and CD loans.
     */
    private static void showOverdue(LibraryService lib) {
        List<Loan> books = lib.getOverdueLoans();
        List<CDLoan> cds = lib.getOverdueCDLoans();

        LOGGER.info(CYAN + "\n--- OVERDUE BOOKS ---" + RESET);
        books.forEach(b -> LOGGER.info(b.toString()));

        LOGGER.info(CYAN + "\n--- OVERDUE CDs ---" + RESET);
        cds.forEach(c -> LOGGER.info(c.toString()));
    }

    /**
     * Triggers sending reminders to all users with overdue items.
     */
    private static void sendReminders(LibraryService lib) {
        lib.sendOverdueReminders();
        LOGGER.info(GREEN + "Reminders sent." + RESET);
    }

    /**
     * Reads an integer from console safely.
     * Repeats until a valid number is entered.
     *
     * @return integer entered by user
     */
    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(input.nextLine());
            } catch (Exception e) {
                LOGGER.info(YELLOW + "Enter number: " + RESET);
            }
        }
    }
}

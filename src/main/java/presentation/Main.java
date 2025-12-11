package presentation;

import domain.Book;
import domain.CDLoan;
import domain.Loan;
import domain.User;
import service.*;
import notification.EmailNotifier;   // 👈 جديد

import java.util.List;
import java.util.Scanner;

/**
 * Console entry point for the Library Management System.
 *
 * Flow:
 *  - Show welcome screen with: Login / Exit
 *  - Login as: Admin / User / Librarian (only Admin fully implemented)
 *  - Admin menu supports:
 *      * Add book
 *      * Add CD
 *      * Unregister user
 *      * Send reminders
 */
public class Main {

    private static final Scanner input = new Scanner(System.in);
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    private static final String ENTER_CHOICE = "Enter choice: ";
    private static final String INVALID = "Invalid.";
    private static final String INVALID_MSG = RED + INVALID + RESET;

    /**
     * Program entry point.
     * Creates all services, loads saved data from files, and starts the main menu flow.
     */
    public static void main(String[] args) {

        // --- Create core services ---
        UserService userService = new UserService();
        BookService bookService = new BookService();
        CDService cdService = new CDService();
        LoanService loanService = new LoanService(bookService, userService);
        CDLoanService cdLoanService = new CDLoanService(bookService, userService);
        ReminderService reminderService = new ReminderService();
        AdminService adminService = new AdminService();

        // 👈 نسجّل EmailNotifier كـ observer حقيقي
        reminderService.addObserver(new EmailNotifier());

        // --- Load persisted data from files ---
        userService.loadUsersFromFile();
        bookService.loadBooksFromFile();
        cdService.loadCDsFromFile();
        loanService.loadLoansFromFile();
        cdLoanService.loadCDLoansFromFile(cdService.getAllCDs());
        adminService.loadAdminsFromFile();

        System.out.println(GREEN + "\nLoaded all data from files successfully.\n" + RESET);

        // --- Start top-level menu (Welcome + Login/Exit) ---
        mainMenu(adminService, new LibraryService(
                userService, bookService, loanService, cdLoanService, reminderService
        ), bookService, cdService, userService);
    }

    // =====================================================
    //  MAIN MENU (WELCOME)
    // =====================================================

    /**
     * Top-level menu:
     *  - Welcome screen
     *  - Login / Exit
     */
    private static void mainMenu(AdminService adminService,
                                 LibraryService library,
                                 BookService bookService,
                                 CDService cdService,
                                 UserService userService) {

        while (true) {
            System.out.println(CYAN + "\n===== WELCOME TO LIBRARY SYSTEM =====" + RESET);
            System.out.println("1) Login");
            System.out.println("2) Exit");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int choice = getInt();

            switch (choice) {
                case 1 -> loginRoleMenu(adminService, library, bookService, cdService, userService);
                case 2 -> {
                    System.out.println(GREEN + "Goodbye!" + RESET);
                    return;
                }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================
    //  LOGIN ROLE MENU (Admin / User / Librarian)
    // =====================================================

    /**
     * Lets the user choose which role to log in as.
     * For now, only Admin path is fully implemented.
     */
    private static void loginRoleMenu(AdminService adminService,
                                      LibraryService library,
                                      BookService bookService,
                                      CDService cdService,
                                      UserService userService) {

        while (true) {
            System.out.println(CYAN + "\n----- LOGIN MENU -----" + RESET);
            System.out.println("1) Login as Admin");
            System.out.println("2) Login as User  (not implemented yet)");
            System.out.println("3) Login as Librarian (not implemented yet)");
            System.out.println("4) Back");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> adminLoginFlow(adminService, library, bookService, cdService, userService);
                case 2 -> System.out.println(RED + "User login is not implemented yet." + RESET);
                case 3 -> System.out.println(RED + "Librarian login is not implemented yet." + RESET);
                case 4 -> {
                    return;
                }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================
    //  ADMIN LOGIN + ADMIN MENU
    // =====================================================

    /**
     * Handles admin authentication and, on failure, allows retrying
     * without going back to the Login Role menu.
     */
    private static void adminLoginFlow(AdminService adminService,
                                       LibraryService library,
                                       BookService bookService,
                                       CDService cdService,
                                       UserService userService) {

        while (true) {
            System.out.print("Admin username (or 0 to go back): ");
            String userName = input.nextLine();

            if ("0".equals(userName)) {
                return;
            }

            System.out.print("Admin password: ");
            String password = input.nextLine();

            if (!adminService.login(userName, password)) {
                System.out.println(RED + "Login failed: invalid admin credentials." + RESET);
                continue;
            }

            System.out.println(GREEN + "Admin logged in successfully." + RESET);

            try {
                adminMenu(adminService, library, bookService, cdService, userService);
            } finally {
                adminService.logout();
                System.out.println(YELLOW + "Admin logged out." + RESET);
            }

            return;
        }
    }

    /**
     * Admin-only menu.
     * Supports:
     *  - Add book
     *  - Add CD
     *  - Unregister user
     *  - Send reminders
     */
    private static void adminMenu(AdminService adminService,
                                  LibraryService library,
                                  BookService bookService,
                                  CDService cdService,
                                  UserService userService) {

        while (true) {
            System.out.println(CYAN + "\n----- ADMIN MENU -----" + RESET);
            System.out.println("1) Add Book");
            System.out.println("2) Add CD");
            System.out.println("3) Unregister User");
            System.out.println("4) Send Reminders");
            System.out.println("5) Back / Logout");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> adminAddBook(bookService);
                case 2 -> adminAddCD(cdService);
                case 3 -> adminUnregisterUser(userService);
                case 4 -> adminSendReminders(library);
                case 5 -> {
                    return;
                }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // -------------------- Admin actions --------------------

    private static void adminAddBook(BookService bookService) {
        System.out.print("Book title: ");
        String title = input.nextLine();
        System.out.print("Book author: ");
        String author = input.nextLine();
        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();

        if (bookService.addBook(title, author, isbn)) {
            System.out.println(GREEN + "Book added and available to borrow." + RESET);
        } else {
            System.out.println(RED + "Cannot add: ISBN already exists." + RESET);
        }
    }

    private static void adminAddCD(CDService cdService) {
        System.out.print("CD title: ");
        String title = input.nextLine();
        System.out.print("CD artist: ");
        String artist = input.nextLine();
        System.out.print("CD ID: ");
        String id = input.nextLine();

        if (cdService.addCD(title, artist, id)) {
            System.out.println(GREEN + "CD added and available to borrow." + RESET);
        } else {
            System.out.println(RED + "Cannot add: CD ID already exists." + RESET);
        }
    }

    private static void adminUnregisterUser(UserService userService) {
        System.out.print("User name to unregister: ");
        String name = input.nextLine();

        User user = userService.findUserByName(name);
        if (user == null) {
            System.out.println(RED + "User not found." + RESET);
            return;
        }

        boolean removed = userService.unregisterUser(user);
        if (removed) {
            System.out.println(GREEN + "User unregistered successfully." + RESET);
        } else {
            System.out.println(RED + "Cannot unregister: user has active loans or unpaid fines." + RESET);
        }
    }

    private static void adminSendReminders(LibraryService library) {
        library.sendOverdueReminders();
        System.out.println(GREEN + "Reminder notifications triggered for overdue users." + RESET);

        List<Loan> overdueBooks = library.getOverdueLoans();
        List<CDLoan> overdueCDs = library.getOverdueCDLoans();
        int total = overdueBooks.size() + overdueCDs.size();
        System.out.println(YELLOW + "Total overdue items: " + total + RESET);
    }

    // =====================================================
    //  UTILITIES
    // =====================================================

    private static int getInt() {
        while (true) {
            try {
                String line = input.nextLine();
                return Integer.parseInt(line.trim());
            } catch (Exception e) {
                System.out.print(YELLOW + "Enter number: " + RESET);
            }
        }
    }
}

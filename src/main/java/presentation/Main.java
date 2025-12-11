package presentation;

import domain.Book;
import domain.CD;
import domain.CDLoan;
import domain.Loan;
import domain.User;
import service.*;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner input = new Scanner(System.in);
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    private static final String ENTER_CHOICE = "Enter choice: ";
    private static final String INVALID_MSG = RED + "Invalid." + RESET;

    public static void main(String[] args) {

        UserService userService = new UserService();
        BookService bookService = new BookService();
        CDService cdService = new CDService();
        LoanService loanService = new LoanService(bookService, userService);
        CDLoanService cdLoanService = new CDLoanService(bookService, userService);
        ReminderService reminderService = new ReminderService();
        AdminService adminService = new AdminService();

        userService.loadUsersFromFile();
        bookService.loadBooksFromFile();
        cdService.loadCDsFromFile();
        loanService.loadLoansFromFile();
        cdLoanService.loadCDLoansFromFile(cdService.getAllCDs());
        adminService.loadAdminsFromFile();

        System.out.println(GREEN + "\nLoaded all data from files successfully.\n" + RESET);

        LibraryService library = new LibraryService(
                userService, bookService, loanService, cdLoanService, reminderService
        );

        mainMenu(adminService, library, bookService, cdService, userService);
    }

    // =====================================================================
    // MAIN MENU
    // =====================================================================

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

    // =====================================================================
    // LOGIN ROLE MENU
    // =====================================================================

    private static void loginRoleMenu(AdminService adminService,
                                      LibraryService library,
                                      BookService bookService,
                                      CDService cdService,
                                      UserService userService) {

        while (true) {
            System.out.println(CYAN + "\n----- LOGIN MENU -----" + RESET);
            System.out.println("1) Login as Admin");
            System.out.println("2) Login as User");
            System.out.println("3) Login as Librarian");
            System.out.println("4) Back");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> adminLoginFlow(adminService, library, bookService, cdService, userService);
                case 2 -> userLoginFlow(userService, library, bookService, cdService);
                case 3 -> librarianMenu(library);
                case 4 -> { return; }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================================
    // ADMIN MENU
    // =====================================================================

    private static void adminLoginFlow(AdminService adminService,
                                       LibraryService library,
                                       BookService bookService,
                                       CDService cdService,
                                       UserService userService) {

        System.out.print("Admin username: ");
        String userName = input.nextLine();

        System.out.print("Admin password: ");
        String password = input.nextLine();

        if (!adminService.login(userName, password)) {
            System.out.println(RED + "Invalid admin credentials." + RESET);
            return;
        }

        while (true) {
            System.out.println(CYAN + "\n----- ADMIN MENU -----" + RESET);
            System.out.println("1) Add Book");
            System.out.println("2) Add CD");
            System.out.println("3) Unregister User");
            System.out.println("4) Send Reminders");
            System.out.println("5) Logout");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> adminAddBook(bookService);
                case 2 -> adminAddCD(cdService);
                case 3 -> adminUnregisterUser(userService);
                case 4 -> {
                    library.sendOverdueReminders();
                    System.out.println(GREEN + "Reminders sent." + RESET);
                }
                case 5 -> {
                    adminService.logout();
                    return;
                }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================================
    // ADMIN ACTIONS (ADDED)
    // =====================================================================

    private static void adminAddBook(BookService bookService) {
        System.out.print("Book title: ");
        String title = input.nextLine();

        System.out.print("Book author: ");
        String author = input.nextLine();

        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();

        if (bookService.addBook(title, author, isbn))
            System.out.println(GREEN + "Book added successfully." + RESET);
        else
            System.out.println(RED + "Cannot add: ISBN already exists." + RESET);
    }

    private static void adminAddCD(CDService cdService) {
        System.out.print("CD title: ");
        String title = input.nextLine();

        System.out.print("CD artist: ");
        String artist = input.nextLine();

        System.out.print("CD ID: ");
        String id = input.nextLine();

        if (cdService.addCD(title, artist, id))
            System.out.println(GREEN + "CD added successfully." + RESET);
        else
            System.out.println(RED + "Cannot add: CD ID already exists." + RESET);
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
        if (removed)
            System.out.println(GREEN + "User unregistered." + RESET);
        else
            System.out.println(RED + "Cannot unregister: user has active loans or unpaid fines." + RESET);
    }

    // =====================================================================
    // LIBRARIAN MENU  (US2.2)
    // =====================================================================

    private static void librarianMenu(LibraryService library) {

        while (true) {
            System.out.println(CYAN + "\n----- LIBRARIAN MENU -----" + RESET);
            System.out.println("1) View overdue book loans (>28 days)");
            System.out.println("2) Back");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> {
                    List<Loan> overdue = library.getOverdueLoans();
                    System.out.println(CYAN + "\n--- OVERDUE BOOKS REPORT ---" + RESET);

                    if (overdue.isEmpty()) {
                        System.out.println(GREEN + "No overdue books." + RESET);
                    } else {
                        for (Loan l : overdue) {
                            System.out.println(YELLOW
                                    + "User: " + l.getUser().getUserName()
                                    + " | Book: " + l.getBook().getTitle()
                                    + " | Due: " + l.getDueDate()
                                    + " | Overdue days: " + l.getOverdueDays()
                                    + RESET);
                        }
                    }
                }
                case 2 -> { return; }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================================
    // USER MENU
    // =====================================================================

    private static void userLoginFlow(UserService userService,
                                      LibraryService library,
                                      BookService bookService,
                                      CDService cdService) {

        System.out.print("User name: ");
        String name = input.nextLine();

        User user = userService.findUserByName(name);
        if (user == null) {
            System.out.println(RED + "User not found." + RESET);
            return;
        }

        while (true) {
            System.out.println(CYAN + "\n----- USER MENU (" + user.getUserName() + ") -----" + RESET);
            System.out.println("1) Search books");
            System.out.println("2) Search CDs");
            System.out.println("3) Borrow book");
            System.out.println("4) Borrow CD");
            System.out.println("5) Return book");
            System.out.println("6) Return CD");
            System.out.println("7) View status");
            System.out.println("8) Pay fine");
            System.out.println("9) Logout");
            System.out.print(YELLOW + ENTER_CHOICE + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> userSearchBooks(bookService);
                case 2 -> userSearchCDs(cdService);
                case 3 -> userBorrowBook(user, library, bookService);
                case 4 -> userBorrowCD(user, library, cdService);
                case 5 -> userReturnBook(user, library, bookService);
                case 6 -> userReturnCD(user, library, cdService);
                case 7 -> userViewStatus(user);
                case 8 -> userPayFine(user, userService);
                case 9 -> { return; }
                default -> System.out.println(INVALID_MSG);
            }
        }
    }

    // =====================================================================
    // USER ACTIONS
    // =====================================================================

    private static void userSearchBooks(BookService bookService) {
        System.out.print("Enter search text: ");
        for (Book b : bookService.search(input.nextLine())) {
            System.out.println(b);
        }
    }

    private static void userSearchCDs(CDService cdService) {
        System.out.print("Enter search text: ");
        for (CD c : cdService.search(input.nextLine())) {
            System.out.println(c);
        }
    }

    private static void userBorrowBook(User user, LibraryService library, BookService bookService) {
        System.out.print("Book ISBN: ");
        Book book = bookService.findBookByISBN(input.nextLine());
        if (book != null && library.borrowBook(user, book))
            System.out.println(GREEN + "Book borrowed." + RESET);
        else
            System.out.println(RED + "Borrow failed." + RESET);
    }

    private static void userBorrowCD(User user, LibraryService library, CDService cdService) {
        System.out.print("CD ID: ");
        CD cd = cdService.findCDById(input.nextLine());
        if (cd != null && library.borrowCD(user, cd))
            System.out.println(GREEN + "CD borrowed." + RESET);
        else
            System.out.println(RED + "Borrow failed." + RESET);
    }

    private static void userReturnBook(User user, LibraryService library, BookService bookService) {
        System.out.print("Book ISBN: ");
        Book book = bookService.findBookByISBN(input.nextLine());
        System.out.println(library.returnBook(user, book)
                ? GREEN + "Returned." + RESET
                : RED + "Return failed." + RESET);
    }

    private static void userReturnCD(User user, LibraryService library, CDService cdService) {
        System.out.print("CD ID: ");
        CD cd = cdService.findCDById(input.nextLine());
        System.out.println(library.returnCD(user, cd)
                ? GREEN + "Returned." + RESET
                : RED + "Return failed." + RESET);
    }

    private static void userViewStatus(User user) {
        System.out.println(user);
    }

    private static void userPayFine(User user, UserService userService) {
        System.out.print("Amount: ");
        user.payFine(Double.parseDouble(input.nextLine()));
        userService.saveUsers();
    }

    // =====================================================================
    // INPUT UTILITY
    // =====================================================================

    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Enter number: ");
            }
        }
    }
}

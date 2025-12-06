package presentation;

import domain.*;
import service.*;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner input = new Scanner(System.in);

    // Colors (console only)
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {

        // Create services
        UserService userService = new UserService();
        BookService bookService = new BookService();
        LoanService loanService = new LoanService(bookService, userService);
        CDLoanService cdLoanService = new CDLoanService(bookService, userService);
        ReminderService reminderService = new ReminderService();

        // IMPORTANT: Load all saved data from files
        userService.loadUsersFromFile();
        bookService.loadBooksFromFile();
        loanService.loadLoansFromFile();
        cdLoanService.loadCDLoansFromFile(List.of()); // modify if needed

        // Library Facade
        LibraryService library = new LibraryService(
                userService, bookService, loanService, cdLoanService, reminderService
        );

        System.out.println(GREEN + "\nLoaded all data from files successfully.\n" + RESET);

        // Main Loop
        while (true) {
            System.out.println(CYAN + "\n========== LIBRARY MENU ==========" + RESET);
            System.out.println("1) Users Menu");
            System.out.println("2) Books Menu");
            System.out.println("3) CDs Menu");
            System.out.println("4) Loans Menu");
            System.out.println("5) Overdue Items");
            System.out.println("6) Send Reminders");
            System.out.println("7) Exit");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int choice = getInt();

            switch (choice) {
                case 1 -> usersMenu(userService);
                case 2 -> booksMenu(bookService);
                case 3 -> cdsMenu();
                case 4 -> loansMenu(library);
                case 5 -> showOverdue(library);
                case 6 -> sendReminders(library);
                case 7 -> {
                    System.out.println(GREEN + "Goodbye!" + RESET);
                    return;
                }
                default -> System.out.println(RED + "Invalid option!" + RESET);
            }
        }
    }

    // ================================================
    // USERS MENU
    // ================================================

    private static void usersMenu(UserService us) {
        while (true) {
            System.out.println(CYAN + "\n----- USERS MENU -----" + RESET);
            System.out.println("1) Add User");
            System.out.println("2) List Users");
            System.out.println("3) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> {
                    System.out.print("Enter name: ");
                    String name = input.nextLine();
                    System.out.print("Enter email: ");
                    String email = input.nextLine();

                    if (us.addUser(name, email))
                        System.out.println(GREEN + "User added." + RESET);
                    else
                        System.out.println(RED + "User already exists!" + RESET);
                }
                case 2 -> {
                    System.out.println(CYAN + "\n--- USERS ---" + RESET);
                    us.getAllUsers().forEach(System.out::println);
                }
                case 3 -> { return; }
                default -> System.out.println(RED + "Invalid." + RESET);
            }
        }
    }

    // ================================================
    // BOOKS MENU
    // ================================================

    private static void booksMenu(BookService bs) {
        while (true) {
            System.out.println(CYAN + "\n----- BOOKS MENU -----" + RESET);
            System.out.println("1) Add Book");
            System.out.println("2) List Books");
            System.out.println("3) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> {
                    System.out.print("Title: ");
                    String title = input.nextLine();
                    System.out.print("Author: ");
                    String author = input.nextLine();
                    System.out.print("ISBN: ");
                    String isbn = input.nextLine();

                    if (bs.addBook(title, author, isbn))
                        System.out.println(GREEN + "Book added." + RESET);
                    else
                        System.out.println(RED + "ISBN already exists!" + RESET);
                }
                case 2 -> {
                    System.out.println(CYAN + "\n--- BOOKS ---" + RESET);
                    bs.getAllBooks().forEach(System.out::println);
                }
                case 3 -> { return; }
                default -> System.out.println(RED + "Invalid." + RESET);
            }
        }
    }

    // ================================================
    // CDs MENU (Optional)
    // ================================================

    private static void cdsMenu() {
        System.out.println(RED + "CD MENU is not implemented because CDs are added manually in code." + RESET);
        System.out.println("Use CDLoanService to borrow CDs directly.");
    }

    // ================================================
    // LOANS MENU
    // ================================================

    private static void loansMenu(LibraryService lib) {
        while (true) {
            System.out.println(CYAN + "\n----- LOANS MENU -----" + RESET);
            System.out.println("1) Borrow Book");
            System.out.println("2) Borrow CD");
            System.out.println("3) Return Book");
            System.out.println("4) Return CD");
            System.out.println("5) List Loans");
            System.out.println("6) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();
            switch (c) {
                case 1 -> borrowBook(lib);
                case 2 -> borrowCD(lib);
                case 3 -> returnBook(lib);
                case 4 -> returnCD(lib);
                case 5 -> listLoans(lib);
                case 6 -> { return; }
                default -> System.out.println(RED + "Invalid." + RESET);
            }
        }
    }

    private static void borrowBook(LibraryService lib) {
        System.out.print("User name: ");
        String uname = input.nextLine();
        User user = lib.findUserByName(uname);

        if (user == null) {
            System.out.println(RED + "User not found!" + RESET);
            return;
        }

        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = lib.findBookByISBN(isbn);

        if (book == null) {
            System.out.println(RED + "Book not found!" + RESET);
            return;
        }

        if (lib.borrowBook(user, book))
            System.out.println(GREEN + "Book borrowed." + RESET);
        else
            System.out.println(RED + "Borrow failed (rules violation)." + RESET);
    }

    private static void returnBook(LibraryService lib) {
        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = lib.findBookByISBN(isbn);

        if (book == null) {
            System.out.println(RED + "Book not found!" + RESET);
            return;
        }

        User borrower = lib.findLoanUser(book);

        if (borrower == null) {
            System.out.println(RED + "Book is not currently borrowed." + RESET);
            return;
        }

        if (lib.returnBook(borrower, book))
            System.out.println(GREEN + "Book returned." + RESET);
        else
            System.out.println(RED + "Return failed." + RESET);
    }

    private static void borrowCD(LibraryService lib) {
        System.out.print("User name: ");
        String uname = input.nextLine();
        System.out.println(RED + "CD borrowing not implemented." + RESET);
    }

    private static void returnCD(LibraryService lib) {
        System.out.println(RED + "CD return not implemented." + RESET);
    }

    private static void listLoans(LibraryService lib) {
        System.out.println(CYAN + "\n--- LOANS ---" + RESET);
        lib.getAllLoans().forEach(System.out::println);
    }

    // ================================================
    // OVERDUE + REMINDERS
    // ================================================

    private static void showOverdue(LibraryService lib) {
        List<Loan> books = lib.getOverdueLoans();
        List<CDLoan> cds = lib.getOverdueCDLoans();

        System.out.println(CYAN + "\n--- OVERDUE BOOKS ---" + RESET);
        books.forEach(System.out::println);

        System.out.println(CYAN + "\n--- OVERDUE CDs ---" + RESET);
        cds.forEach(System.out::println);
    }

    private static void sendReminders(LibraryService lib) {
        lib.sendOverdueReminders();
        System.out.println(GREEN + "Reminders sent." + RESET);
    }

    private static int getInt() {
        while (true) {
            try {
                return Integer.parseInt(input.nextLine());
            } catch (Exception e) {
                System.out.print(YELLOW + "Enter number: " + RESET);
            }
        }
    }
}

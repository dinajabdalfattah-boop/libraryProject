package presentation;

import domain.Book;
import domain.User;
import domain.Loan;
import service.LibraryService;
import service.ReminderService;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner input = new Scanner(System.in);

    // ANSI COLORS
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    // Admin password (for Phase 1 simplicity)
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "1234";
    private static boolean adminLoggedIn = false;

    public static void main(String[] args) {

        ReminderService reminderService = new ReminderService();
        LibraryService library = new LibraryService(reminderService, true);

        while (true) {
            System.out.println(CYAN + "\n===== LIBRARY SYSTEM =====" + RESET);
            System.out.println("1) Admin Login");
            System.out.println("2) Users Menu");
            System.out.println("3) Books Menu");
            System.out.println("4) Loans Menu");
            System.out.println("5) Show Overdue");
            System.out.println("6) Exit");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int choice = getInt();

            switch (choice) {
                case 1 -> adminLogin();
                case 2 -> usersMenu(library);
                case 3 -> booksMenu(library);
                case 4 -> loansMenu(library);
                case 5 -> showOverdue(library);
                case 6 -> {
                    System.out.println(GREEN + "Goodbye!" + RESET);
                    return;
                }
                default -> System.out.println(RED + "Invalid choice!" + RESET);
            }
        }
    }

    // ===================== ADMIN LOGIN =========================

    private static void adminLogin() {
        System.out.print("Username: ");
        String u = input.nextLine();
        System.out.print("Password: ");
        String p = input.nextLine();

        if (u.equals(ADMIN_USER) && p.equals(ADMIN_PASS)) {
            adminLoggedIn = true;
            System.out.println(GREEN + "Admin logged in successfully!" + RESET);
        } else {
            System.out.println(RED + "Wrong credentials!" + RESET);
        }
    }

    // ===================== USERS MENU =========================

    private static void usersMenu(LibraryService library) {
        while (true) {
            System.out.println(CYAN + "\n===== USERS =====" + RESET);
            System.out.println("1) Add User");
            System.out.println("2) List Users");
            System.out.println("3) Remove User");
            System.out.println("4) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> addUser(library);
                case 2 -> listUsers(library);
                case 3 -> removeUser(library);
                case 4 -> { return; }
                default -> System.out.println(RED + "Invalid option!" + RESET);
            }
        }
    }

    private static void addUser(LibraryService library) {
        if (!adminLoggedIn) {
            System.out.println(RED + "Admin must be logged in!" + RESET);
            return;
        }

        System.out.print("Enter user name: ");
        String name = input.nextLine();
        System.out.print("Enter email: ");
        String email = input.nextLine();

        boolean ok = library.addUser(new User(name, email));
        if (ok) System.out.println(GREEN + "User added!" + RESET);
        else System.out.println(RED + "User already exists!" + RESET);
    }

    private static void listUsers(LibraryService library) {
        List<User> users = library.getAllUsers();

        System.out.println(CYAN + "\n--- USERS LIST ---" + RESET);
        for (User u : users) {
            System.out.println(u.getUserName() + " | Email: " + u.getEmail() +
                    " | Fine: " + u.getFineBalance());
        }
    }

    private static void removeUser(LibraryService library) {
        if (!adminLoggedIn) {
            System.out.println(RED + "Admin must be logged in!" + RESET);
            return;
        }

        System.out.print("Enter user name to remove: ");
        String name = input.nextLine();

        User u = library.findUserByName(name);
        if (u == null) {
            System.out.println(RED + "User not found!" + RESET);
            return;
        }

        boolean ok = library.unregisterUser(u);

        if (ok) System.out.println(GREEN + "User removed!" + RESET);
        else System.out.println(RED + "User cannot be removed (active loans or fines)." + RESET);
    }

    // ===================== BOOKS MENU =========================

    private static void booksMenu(LibraryService library) {
        while (true) {
            System.out.println(CYAN + "\n===== BOOKS =====" + RESET);
            System.out.println("1) Add Book");
            System.out.println("2) List Books");
            System.out.println("3) Search Book");
            System.out.println("4) Borrow Book");
            System.out.println("5) Return Book");
            System.out.println("6) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> addBook(library);
                case 2 -> listBooks(library);
                case 3 -> searchBook(library);
                case 4 -> borrowBook(library);
                case 5 -> returnBook(library);
                case 6 -> { return; }
                default -> System.out.println(RED + "Invalid!" + RESET);
            }
        }
    }

    private static void addBook(LibraryService library) {
        if (!adminLoggedIn) {
            System.out.println(RED + "Admin must be logged in!" + RESET);
            return;
        }

        System.out.print("Title: ");
        String title = input.nextLine();
        System.out.print("Author: ");
        String author = input.nextLine();
        System.out.print("ISBN: ");
        String isbn = input.nextLine();

        boolean ok = library.addBook(new Book(title, author, isbn));

        if (ok) System.out.println(GREEN + "Book added!" + RESET);
        else System.out.println(RED + "Duplicate ISBN!" + RESET);
    }

    private static void listBooks(LibraryService library) {
        System.out.println(CYAN + "\n--- BOOKS LIST ---" + RESET);
        library.getAllBooks().forEach(System.out::println);
    }

    private static void searchBook(LibraryService library) {
        System.out.print("Search keyword: ");
        String kw = input.nextLine();

        List<Book> found = library.getAllBooks().stream()
                .filter(b -> b.getTitle().contains(kw)
                        || b.getAuthor().contains(kw)
                        || b.getIsbn().contains(kw))
                .toList();

        if (found.isEmpty()) {
            System.out.println(RED + "No matching books!" + RESET);
        } else {
            System.out.println(GREEN + "Found books:" + RESET);
            found.forEach(System.out::println);
        }
    }

    private static void borrowBook(LibraryService library) {
        System.out.print("User name: ");
        String uname = input.nextLine();
        User user = library.findUserByName(uname);

        if (user == null) {
            System.out.println(RED + "User not found!" + RESET);
            return;
        }

        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = library.findBookByISBN(isbn);

        if (book == null) {
            System.out.println(RED + "Book not found!" + RESET);
            return;
        }

        boolean ok = library.borrowBook(user, book);

        if (ok) System.out.println(GREEN + "Book borrowed!" + RESET);
        else System.out.println(RED + "Cannot borrow (rules violation)." + RESET);
    }

    private static void returnBook(LibraryService library) {
        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();

        Book b = library.findBookByISBN(isbn);

        if (b == null) {
            System.out.println(RED + "Book not found!" + RESET);
            return;
        }

        b.returnBook();

        System.out.println(GREEN + "Book returned!" + RESET);
    }

    // ===================== LOANS MENU =========================

    private static void loansMenu(LibraryService library) {
        while (true) {
            System.out.println(CYAN + "\n===== LOANS =====" + RESET);
            System.out.println("1) List Loans");
            System.out.println("2) Send Reminders");
            System.out.println("3) Back");
            System.out.print(YELLOW + "Enter choice: " + RESET);

            int c = getInt();

            switch (c) {
                case 1 -> listLoans(library);
                case 2 -> sendReminders(library);
                case 3 -> { return; }
                default -> System.out.println(RED + "Invalid!" + RESET);
            }
        }
    }

    private static void listLoans(LibraryService library) {
        System.out.println(CYAN + "\n--- ACTIVE LOANS ---" + RESET);
        List<Loan> loans = library.getAllLoans();
        loans.forEach(l -> {
            System.out.println(l.getUser().getUserName() + " → " +
                    l.getBook().getTitle() +
                    " | Borrow: " + l.getBorrowDate() +
                    " | Due: " + l.getDueDate());
        });
    }

    private static void sendReminders(LibraryService library) {
        library.sendOverdueReminders();
        System.out.println(GREEN + "Reminders sent!" + RESET);
    }

    // ===================== OVERDUE =========================

    private static void showOverdue(LibraryService library) {
        List<Loan> overdue = library.getOverdueLoans();

        System.out.println(CYAN + "\n--- OVERDUE LOANS ---" + RESET);
        for (Loan l : overdue) {
            System.out.println(l.getUser().getUserName() + " | " +
                    l.getBook().getTitle() + " | Due: " + l.getDueDate());
        }
    }

    // ===================== Helper =========================

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

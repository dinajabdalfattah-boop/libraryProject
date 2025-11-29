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

    public static void main(String[] args) {
        ReminderService reminderService = new ReminderService();
        LibraryService library = new LibraryService(reminderService, true); // هون بدنا يحمل من الملفات

    ;

        while (true) {
            System.out.println("\n===== LIBRARY SYSTEM =====");
            System.out.println("1) Users Menu");
            System.out.println("2) Books Menu");
            System.out.println("3) Loans Menu");
            System.out.println("4) Show Overdue");
            System.out.println("5) Exit");
            System.out.print("Enter choice: ");

            int choice = getInt();

            switch (choice) {
                case 1 -> usersMenu(library);
                case 2 -> booksMenu(library);
                case 3 -> loansMenu(library);
                case 4 -> showOverdue(library);
                case 5 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    // ===================== USERS MENU =========================

    private static void usersMenu(LibraryService library) {
        while (true) {
            System.out.println("\n===== USERS =====");
            System.out.println("1) Add User");
            System.out.println("2) List Users");
            System.out.println("3) Remove User");
            System.out.println("4) Back");
            System.out.print("Enter choice: ");

            int c = getInt();

            switch (c) {
                case 1 -> addUser(library);
                case 2 -> listUsers(library);
                case 3 -> removeUser(library);
                case 4 -> { return; }
                default -> System.out.println("Invalid!");
            }
        }
    }

    private static void addUser(LibraryService library) {
        System.out.print("Enter user name: ");
        String name = input.nextLine();

        boolean ok = library.addUser(new User(name));
        if (ok) System.out.println("User added!");
        else System.out.println("User already exists!");
    }

    private static void listUsers(LibraryService library) {
        List<User> users = library.getAllUsers();

        System.out.println("\n--- USERS LIST ---");
        for (User u : users) {
            System.out.println(u.getUserName() + " | Fine: " + u.getFineBalance());
        }
    }

    private static void removeUser(LibraryService library) {
        System.out.print("Enter user name to remove: ");
        String name = input.nextLine();

        User u = library.findUserByName(name);
        if (u == null) {
            System.out.println("User not found!");
            return;
        }

        boolean ok = library.unregisterUser(u);

        if (ok) System.out.println("User removed!");
        else System.out.println("User cannot be removed (active loans or fines).");
    }

    // ===================== BOOKS MENU =========================

    private static void booksMenu(LibraryService library) {
        while (true) {
            System.out.println("\n===== BOOKS =====");
            System.out.println("1) Add Book");
            System.out.println("2) List Books");
            System.out.println("3) Borrow Book");
            System.out.println("4) Return Book");
            System.out.println("5) Back");
            System.out.print("Enter choice: ");

            int c = getInt();

            switch (c) {
                case 1 -> addBook(library);
                case 2 -> listBooks(library);
                case 3 -> borrowBook(library);
                case 4 -> returnBook(library);
                case 5 -> { return; }
                default -> System.out.println("Invalid!");
            }
        }
    }

    private static void addBook(LibraryService library) {
        System.out.print("Title: ");
        String title = input.nextLine();
        System.out.print("Author: ");
        String author = input.nextLine();
        System.out.print("ISBN: ");
        String isbn = input.nextLine();

        boolean ok = library.addBook(new Book(title, author, isbn));

        if (ok) System.out.println("Book added!");
        else System.out.println("Duplicate ISBN!");
    }

    private static void listBooks(LibraryService library) {
        List<Book> books = library.getAllBooks();

        System.out.println("\n--- BOOKS LIST ---");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void borrowBook(LibraryService library) {
        System.out.print("User name: ");
        String uname = input.nextLine();
        User user = library.findUserByName(uname);

        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();
        Book book = library.findBookByISBN(isbn);

        if (book == null) {
            System.out.println("Book not found!");
            return;
        }

        boolean ok = library.borrowBook(user, book);

        if (ok) System.out.println("Book borrowed!");
        else System.out.println("Cannot borrow (rules violation).");
    }

    private static void returnBook(LibraryService library) {
        System.out.print("Book ISBN: ");
        String isbn = input.nextLine();

        Book b = library.findBookByISBN(isbn);

        if (b == null) {
            System.out.println("Book not found!");
            return;
        }

        b.returnBook();

        System.out.println("Book returned!");
    }

    // ===================== LOANS MENU =========================

    private static void loansMenu(LibraryService library) {
        while (true) {
            System.out.println("\n===== LOANS =====");
            System.out.println("1) List Loans");
            System.out.println("2) Send Reminders");
            System.out.println("3) Back");
            System.out.print("Enter choice: ");

            int c = getInt();

            switch (c) {
                case 1 -> listLoans(library);
                case 2 -> sendReminders(library);
                case 3 -> { return; }
                default -> System.out.println("Invalid!");
            }
        }
    }

    private static void listLoans(LibraryService library) {
        List<Loan> loans = library.getAllLoans();

        System.out.println("\n--- ACTIVE LOANS ---");
        for (Loan l : loans) {
            System.out.println(l.getUser().getUserName() + " → " +
                    l.getBook().getTitle() +
                    " | Borrow: " + l.getBorrowDate() +
                    " | Due: " + l.getDueDate());
        }
    }

    private static void sendReminders(LibraryService library) {
        library.sendOverdueReminders();
        System.out.println("Reminders sent (check MockNotifier).");
    }

    // ===================== OVERDUE =========================

    private static void showOverdue(LibraryService library) {
        List<Loan> overdue = library.getOverdueLoans();

        System.out.println("\n--- OVERDUE LOANS ---");
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
                System.out.print("Enter number: ");
            }
        }
    }
}

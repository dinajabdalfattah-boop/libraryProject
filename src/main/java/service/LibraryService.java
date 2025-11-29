package service;

import domain.Book;
import domain.Loan;
import domain.User;
import utils.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class LibraryService {

    private final ReminderService reminderService;

    private final List<User> users = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();

    private static final String USERS_FILE = "src/main/resources/data/users.txt";
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";
    private static final String LOANS_FILE = "src/main/resources/data/loans.txt";

    // 👈 هذا اللي رح نستخدمه في الـ tests (ما بعمل تحميل من الملفات)
    public LibraryService(ReminderService reminderService) {
        this(reminderService, false);
    }

    // 👈 هذا اللي رح نستخدمه في الـ Main (يختار إذا يحمّل من الملفات أو لا)
    public LibraryService(ReminderService reminderService, boolean loadFromFiles) {
        this.reminderService = reminderService;
        if (loadFromFiles) {
            loadAll();
        }
    }

    // ========================
    // LOAD EVERYTHING
    // ========================

    public void loadAll() {
        loadUsers();
        loadBooks();
        loadLoans();
    }

    // ========================
    // USERS
    // ========================

    private void loadUsers() {
        users.clear();

        List<String> lines = FileManager.readLines(USERS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            String name  = p[0];
            String email = p[1];
            double fine  = Double.parseDouble(p[2]);

            User u = new User(name, email);
            u.setFineBalance(fine);

            users.add(u);
        }
    }

    // ========================
    // BOOKS
    // ========================

    private void loadBooks() {
        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            Book b = new Book(p[0], p[1], p[2]);

            boolean available = Boolean.parseBoolean(p[3]);
            b.setAvailable(available);

            if (!available) {
                if (!"null".equals(p[4])) b.setBorrowDate(LocalDate.parse(p[4]));
                if (!"null".equals(p[5])) b.setDueDate(LocalDate.parse(p[5]));
            }

            books.add(b);
        }
    }

    // ========================
    // LOANS
    // ========================

    private void loadLoans() {
        loans.clear();

        List<String> lines = FileManager.readLines(LOANS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            User u = findUserByName(p[0]);
            Book b = findBookByISBN(p[1]);
            LocalDate due = LocalDate.parse(p[2]);

            if (u != null && b != null) {
                Loan loan = new Loan(u, b);
                loan.setDueDate(due);
                loans.add(loan);
            }
        }
    }

    // ========================
    // FINDERS
    // ========================

    public User findUserByName(String name) {
        return users.stream()
                .filter(u -> u.getUserName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Book findBookByISBN(String isbn) {
        return books.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst()
                .orElse(null);
    }

    // ========================
    // CORE LOGIC (used in tests)
    // ========================

    public boolean addBook(Book book) {
        // منع تكرار الـ ISBN
        if (books.stream().anyMatch(b -> b.getIsbn().equals(book.getIsbn()))) {
            return false;
        }
        books.add(book);
        return true;
    }

    public boolean addUser(User user) {
        // منع تكرار الـ name
        if (users.stream().anyMatch(u -> u.getUserName().equals(user.getUserName()))) {
            return false;
        }
        users.add(user);
        return true;
    }

    public boolean borrowBook(User user, Book book) {
        if (!users.contains(user)) return false;
        if (!books.contains(book)) return false;

        try {
            // هذي فيها كل قواعد: غرامة + كتب متأخرة
            user.borrowBook(book);

            Loan loan = new Loan(user, book);
            loans.add(loan);
            return true;
        } catch (IllegalStateException ex) {
            // إذا عنده غرامة أو كتاب متأخر
            return false;
        }
    }

    public boolean unregisterUser(User user) {
        if (!users.contains(user)) return false;

        // لو عنده قروض فعّالة أو غرامة
        if (!user.canBeUnregistered()) return false;

        boolean hasActiveLoan = loans.stream()
                .anyMatch(l -> l.getUser().equals(user) && !l.getBook().isAvailable());

        if (hasActiveLoan) return false;

        users.remove(user);
        return true;
    }

    public List<Loan> getOverdueLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans) {
            if (l.isOverdue()) {
                result.add(l);
            }
        }
        return result;
    }

    public void sendOverdueReminders() {
        List<Loan> overdue = getOverdueLoans();
        reminderService.sendReminders(overdue);
    }

    // ========================
    // GET LISTS (used by tests)
    // ========================

    public List<User> getAllUsers() {
        return users;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public List<Loan> getAllLoans() {
        return loans;
    }
}

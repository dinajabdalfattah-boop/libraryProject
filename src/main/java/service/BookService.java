package service;

import domain.Book;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing books in the library system.
 * This class supports adding books, searching, loading/saving data,
 * and retrieving books by ISBN.
 */
public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";
    private static final String NULL_LITERAL = "null";

    /**
     * Adds a new book to the system if the ISBN is not already registered.
     * The updated list is saved to the storage file after a successful insert.
     *
     * @param title  the book title
     * @param author the book author
     * @param isbn   the unique ISBN of the book
     * @return true if the book was added successfully, false if a book with the same ISBN already exists
     */
    public boolean addBook(String title, String author, String isbn) {
        if (findBookByISBN(isbn) != null) return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();
        return true;
    }

    /**
     * Saves all books to the storage file using a comma-separated format:
     * title,author,isbn,available,borrowDate,dueDate.
     * Dates are stored as ISO strings, and missing values are stored as "null".
     */
    public void saveBooksToFile() {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            lines.add(toCsvLine(
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    b.isAvailable(),
                    b.getBorrowDate(),
                    b.getDueDate()
            ));
        }
        FileManager.writeLines(BOOKS_FILE, lines);
    }

    /**
     * Loads books from the storage file into memory.
     * Invalid or incomplete lines are ignored.
     */
    public void loadBooksFromFile() {
        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 3) continue;

            Book b = new Book(p[0], p[1], p[2]);

            b.setAvailable(parseBooleanStrict(getPart(p, 3)));

            b.setBorrowDate(parseDateOrNull(getPart(p, 4)));
            b.setDueDate(parseDateOrNull(getPart(p, 5)));

            books.add(b);
        }
    }

    /**
     * Searches for books using a keyword.
     * Matching rules:
     * - if keyword is blank, returns all books
     * - title contains keyword (case-insensitive)
     * - author matches keyword (case-insensitive)
     * - ISBN matches keyword (exact match)
     *
     * @param keyword the search keyword (must not be null)
     * @return a list of matching books
     * @throws NullPointerException if keyword is null
     */
    public List<Book> search(String keyword) {
        if (keyword == null) throw new NullPointerException("keyword is null");

        keyword = keyword.trim();
        if (keyword.isEmpty()) return new ArrayList<>(books);

        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (matchesKeyword(keyword, b.getTitle(), b.getAuthor(), b.getIsbn())) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Finds a book by its ISBN.
     *
     * @param isbn the ISBN to search for
     * @return the matching Book if found, otherwise null
     */
    public Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) return b;
        }
        return null;
    }

    /**
     * Returns all books currently loaded in memory.
     *
     * @return the list of all books
     */
    public List<Book> getAllBooks() {
        return books;
    }

    /**
     * Builds a CSV line for persisting a Book entry.
     *
     * @param title      book title
     * @param author     book author
     * @param isbn       book ISBN
     * @param available  availability flag
     * @param borrowDate borrow date (nullable)
     * @param dueDate    due date (nullable)
     * @return a comma-separated string representing the book
     */
    private static String toCsvLine(String title, String author, String isbn,
                                    boolean available, LocalDate borrowDate, LocalDate dueDate) {
        return String.join(",",
                title,
                author,
                isbn,
                String.valueOf(available),
                dateToStringOrNull(borrowDate),
                dateToStringOrNull(dueDate)
        );
    }

    /**
     * Converts a LocalDate into a storable string.
     *
     * @param d the date value (nullable)
     * @return ISO date string, or "null" if the value is null
     */
    private static String dateToStringOrNull(LocalDate d) {
        return d == null ? NULL_LITERAL : d.toString();
    }

    /**
     * Safely returns a part from a split array.
     *
     * @param parts the split array
     * @param index required index
     * @return the element at index if present, otherwise null
     */
    private static String getPart(String[] parts, int index) {
        return parts.length > index ? parts[index] : null;
    }

    /**
     * Parses a boolean value from a string in a strict and safe way.
     * Null, blank, or the literal "null" are treated as false.
     *
     * @param s the string to parse
     * @return the parsed boolean value, or false for null/blank/"null"
     */
    private boolean parseBooleanStrict(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase(NULL_LITERAL)) return false;
        return Boolean.parseBoolean(s);
    }

    /**
     * Parses a LocalDate from a string.
     * Null, blank, or the literal "null" are treated as null.
     *
     * @param s the string to parse
     * @return the parsed LocalDate, or null for null/blank/"null"
     */
    private LocalDate parseDateOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase(NULL_LITERAL)) return null;
        return LocalDate.parse(s);
    }

    /**
     * Checks whether a book matches the given keyword according to the same search rules.
     *
     * @param keyword the raw keyword (already trimmed by caller)
     * @param title   book title
     * @param author  book author
     * @param isbn    book ISBN
     * @return true if any matching rule applies, otherwise false
     */
    private static boolean matchesKeyword(String keyword, String title, String author, String isbn) {
        String lower = keyword.toLowerCase();

        if (title != null && title.toLowerCase().contains(lower)) return true;
        if (author != null && author.equalsIgnoreCase(keyword)) return true;
        return isbn != null && isbn.equals(keyword);
    }
}

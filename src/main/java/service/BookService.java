package service;

import domain.Book;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";

    public boolean addBook(String title, String author, String isbn) {

        if (findBookByISBN(isbn) != null)
            return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();
        return true;
    }

    public void saveBooksToFile() {

        List<String> lines = new ArrayList<>();

        for (Book b : books) {
            String line = String.join(",",
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    String.valueOf(b.isAvailable()),
                    b.getBorrowDate() == null ? "null" : b.getBorrowDate().toString(),
                    b.getDueDate() == null ? "null" : b.getDueDate().toString()
            );
            lines.add(line);
        }

        FileManager.writeLines(BOOKS_FILE, lines);
    }

    // null/blank/"null" => false
    private boolean parseBooleanStrict(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.isEmpty()) return false;
        if (s.equalsIgnoreCase("null")) return false;
        return Boolean.parseBoolean(s);
    }

    // null/blank/"null" => null
    private LocalDate parseDateOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(s);
    }

    public void loadBooksFromFile() {

        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank())
                continue;

            String[] p = line.split(",");

            if (p.length < 3)
                continue;

            Book b = new Book(p[0], p[1], p[2]);

            // ✅ availability from file (null treated as false)
            String availableStr = (p.length > 3) ? p[3] : null;
            boolean available = parseBooleanStrict(availableStr);
            b.setAvailable(available);

            // ✅ IMPORTANT: if available==true => IGNORE dates and force null
            if (available) {
                b.setBorrowDate(null);
                b.setDueDate(null);
            } else {
                String borrowStr = (p.length > 4) ? p[4] : null;
                String dueStr = (p.length > 5) ? p[5] : null;
                b.setBorrowDate(parseDateOrNull(borrowStr));
                b.setDueDate(parseDateOrNull(dueStr));
            }

            books.add(b);
        }
    }

    public List<Book> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

        keyword = keyword.trim();
        if (keyword.isEmpty())
            return new ArrayList<>(books);

        String keyLower = keyword.toLowerCase();

        List<Book> results = new ArrayList<>();

        for (Book b : books) {

            if (b.getTitle().toLowerCase().contains(keyLower)) {
                results.add(b);
                continue;
            }

            if (b.getAuthor().equalsIgnoreCase(keyword)) {
                results.add(b);
                continue;
            }

            if (b.getIsbn().equals(keyword)) {
                results.add(b);
            }
        }

        return results;
    }

    public Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn))
                return b;
        }
        return null;
    }

    public List<Book> getAllBooks() {
        return books;
    }
}

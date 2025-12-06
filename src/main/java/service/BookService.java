package service;

import domain.Book;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";

    // ---------------------------------------------------------
    // ADD BOOK
    // ---------------------------------------------------------

    /** Adds a book if ISBN is unique */
    public boolean addBook(String title, String author, String isbn) {

        if (findBookByISBN(isbn) != null)
            return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();
        return true;
    }

    // ---------------------------------------------------------
    // SAVE / LOAD
    // ---------------------------------------------------------

    private void saveBooksToFile() {
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

    public void loadBooksFromFile() {
        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 6) continue;  // NEW: لمنع ArrayIndexOutOfBounds

            Book b = new Book(p[0], p[1], p[2]);

            boolean available = Boolean.parseBoolean(p[3]);
            b.setAvailable(available);

            if (!available) {
                if (!p[4].equals("null"))
                    b.setBorrowDate(LocalDate.parse(p[4]));
                if (!p[5].equals("null"))
                    b.setDueDate(LocalDate.parse(p[5]));
            }

            books.add(b);
        }
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    public List<Book> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

        // NEW: keyword contains only spaces → return all books
        if (keyword.trim().isEmpty())
            return new ArrayList<>(books);

        keyword = keyword.toLowerCase();
        List<Book> results = new ArrayList<>();

        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword) ||
                    b.getAuthor().toLowerCase().contains(keyword) ||
                    b.getIsbn().toLowerCase().contains(keyword)) {
                results.add(b);
            }
        }

        return results;
    }

    // ---------------------------------------------------------
    // FIND BY ISBN
    // ---------------------------------------------------------

    public Book findBookByISBN(String isbn) {
        for (Book b : books)
            if (b.getIsbn().equals(isbn))
                return b;
        return null;
    }

    // ---------------------------------------------------------
    // GET ALL BOOKS
    // ---------------------------------------------------------

    public List<Book> getAllBooks() {
        return books;
    }
}

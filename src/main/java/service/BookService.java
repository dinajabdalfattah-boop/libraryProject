package service;

import domain.Book;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing all book-related operations:
 *  - Add books
 *  - Load books from file
 *  - Save books to file
 *  - Search books
 *  - Find book by ISBN
 */
public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";

    /**
     * Adds a new book if ISBN does not already exist.
     * Automatically saves the updated book list to file.
     */
    public boolean addBook(String title, String author, String isbn) {

        if (findBookByISBN(isbn) != null)
            return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();   // 🔥 حفظ تلقائي
        return true;
    }

    /**
     * Saves all books to the file in this format:
     * title,author,isbn,isAvailable,borrowDate,dueDate
     */
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

    /**
     * Loads books from the file, ignores invalid lines.
     * Books are initially loaded as AVAILABLE, then LoanService
     * will update active borrowed books when loading loans.
     */
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

            // Always reset state — LoanService fixes borrowed ones later
            b.setAvailable(true);
            b.setBorrowDate(null);
            b.setDueDate(null);

            books.add(b);
        }
    }

    /**
     * Returns list of books that match keyword in:
     *  - title
     *  - author
     *  - ISBN
     */
    public List<Book> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

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

    /**
     * Search for a book using ISBN.
     */
    public Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn))
                return b;
        }
        return null;
    }

    /**
     * Returns all currently loaded books.
     */
    public List<Book> getAllBooks() {
        return books;
    }
}

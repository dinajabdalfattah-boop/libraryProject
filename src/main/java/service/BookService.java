package service;

import domain.Book;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * This service manages all book-related operations in the library system.
 * It handles adding books, searching for books, loading/saving book data
 * to a file, and finding books by ISBN. The class keeps all books in memory
 * and synchronizes them with a text file when changes occur.
 */
public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";

    /**
     * Adds a new book to the system only if the ISBN is unique.
     * When a new book is successfully added, the updated list
     * is saved to the persistent storage file.
     *
     * @param title  the book title
     * @param author the author name
     * @param isbn   unique ISBN identifier
     * @return true if the book was added, false if the ISBN already exists
     */
    public boolean addBook(String title, String author, String isbn) {

        if (findBookByISBN(isbn) != null)
            return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();
        return true;
    }

    /**
     * Saves the current list of books into the books file.
     * Each book is written as a single line containing:
     * title, author, isbn, availability, borrowDate, dueDate.
     *
     * If the file does not exist, FileManager will create it.
     */
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

    /**
     * Loads all books from the storage file into memory.
     * Reconstructs each book and restores its borrowing status
     * (borrow date and due date) if the book was previously borrowed.
     *
     * Invalid or incomplete lines are ignored to avoid errors.
     */
    public void loadBooksFromFile() {

        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank())
                continue;

            String[] p = line.split(",");

            // نتأكد إن فيه على الأقل 3 حقول (title, author, isbn)
            if (p.length < 3)
                continue;

            // نقرأ بس البيانات الأساسية للكتاب
            Book b = new Book(p[0], p[1], p[2]);

            // 👈 مهم: نطنّش حالة الإعارة المخزّنة في الملف
            // نخليه دائماً متاح، وبعدين LoanService لما يقرأ loans
            // هو اللي برجع يعيّر الكتب النشطة
            b.setAvailable(true);
            b.setBorrowDate(null);
            b.setDueDate(null);

            books.add(b);
        }
    }

    /**
     * Searches for books using a keyword. The search is performed on:
     * - title
     * - author
     * - ISBN
     *
     * If the keyword contains only spaces, all books are returned.
     *
     * @param keyword the term to search for
     * @return a list of matching books
     * @throws NullPointerException if keyword is null
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
     * Searches for a book using its ISBN.
     *
     * @param isbn the unique ISBN to search for
     * @return the matching Book object, or null if not found
     */
    public Book findBookByISBN(String isbn) {
        for (Book b : books)
            if (b.getIsbn().equals(isbn))
                return b;
        return null;
    }

    /**
     * Returns the internal list of all books currently stored.
     *
     * @return the list of books
     */
    public List<Book> getAllBooks() {
        return books;
    }
}

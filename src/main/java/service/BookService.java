package service;

import domain.Book;
import file.FileManager;

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

            b.setAvailable(true);
            b.setBorrowDate(null);
            b.setDueDate(null);

            books.add(b);
        }
    }

    /**
     * search() — EXACT for author & ISBN, PARTIAL for title
     */
    public List<Book> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

        keyword = keyword.trim();
        if (keyword.isEmpty())
            return new ArrayList<>(books);

        String keyLower = keyword.toLowerCase();

        List<Book> results = new ArrayList<>();

        for (Book b : books) {

            // title → partial match
            if (b.getTitle().toLowerCase().contains(keyLower)) {
                results.add(b);
                continue;
            }

            // author → exact only
            if (b.getAuthor().equalsIgnoreCase(keyword)) {
                results.add(b);
                continue;
            }

            // ISBN → exact only
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

package service;

import domain.Book;
import utils.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private final List<Book> books = new ArrayList<>();
    private static final String BOOKS_FILE = "src/main/resources/data/books.txt";

    public boolean addBook(String title, String author, String isbn) {

        for (Book b : books)
            if (b.getIsbn().equals(isbn))
                return false;

        Book book = new Book(title, author, isbn);
        books.add(book);

        saveBooksToFile();
        return true;
    }

    private void saveBooksToFile() {
        List<String> lines = new ArrayList<>();

        for (Book b : books) {
            String line =
                    b.getTitle() + "," +
                            b.getAuthor() + "," +
                            b.getIsbn() + "," +
                            b.isAvailable() + "," +
                            (b.getBorrowDate() == null ? "null" : b.getBorrowDate()) + "," +
                            (b.getDueDate() == null ? "null" : b.getDueDate());

            lines.add(line);
        }

        FileManager.writeLines(BOOKS_FILE, lines);
    }

    public void loadBooksFromFile() {
        books.clear();

        List<String> lines = FileManager.readLines(BOOKS_FILE);

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            Book b = new Book(p[0], p[1], p[2]);

            boolean available = Boolean.parseBoolean(p[3]);
            b.setAvailable(available);

            if (!available) {
                if (!p[4].equals("null")) b.setBorrowDate(LocalDate.parse(p[4]));
                if (!p[5].equals("null")) b.setDueDate(LocalDate.parse(p[5]));
            }

            books.add(b);
        }
    }

    public List<Book> getAllBooks() {
        return books;
    }

    public Book findBookByISBN(String isbn) {
        for (Book b : books)
            if (b.getIsbn().equals(isbn))
                return b;

        return null;
    }

    public boolean borrowBook(String isbn) {
        Book b = findBookByISBN(isbn);
        if (b == null) return false;

        try {
            b.borrowBook();
            saveBooksToFile();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean returnBook(String isbn) {
        Book b = findBookByISBN(isbn);
        if (b == null) return false;

        b.returnBook();
        saveBooksToFile();
        return true;
    }

    public boolean isBookOverdue(String isbn) {
        Book b = findBookByISBN(isbn);
        return b != null && b.isOverdue();
    }
    public void searchBook(String keyword) {

        for (Book b : books) {
            if (b.getTitle().equalsIgnoreCase(keyword) ||
                    b.getAuthor().equalsIgnoreCase(keyword) ||
                    b.getIsbn().equalsIgnoreCase(keyword)) {

                System.out.println("Found book: " + b);
                return; // found, end
            }
        }

        // If not found
        System.out.println("No book found for keyword: " + keyword);
    }

}

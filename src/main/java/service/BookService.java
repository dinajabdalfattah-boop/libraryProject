package service;

import domain.Book;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private final List<Book> books = new ArrayList<>();

    public boolean addBook(String title, String author, String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) {
                return false;
            }
        }
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        return true;
    }

    public List<Book> searchBook(String key) {
        List<Book> results = new ArrayList<>();

        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(key.toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(key.toLowerCase()) ||
                    b.getIsbn().contains(key)) {

                results.add(b);
            }
        }
        return results;
    }

    public List<Book> getAllBooks() {
        return books;
    }

    // ---------------- Sprint 2 methods ----------------

    public boolean borrowBook(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) return false;

        try {
            book.borrowBook();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public boolean returnBook(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) return false;

        book.returnBook();
        return true;
    }

    public boolean isBookOverdue(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) return false;

        return book.isOverdue();
    }

    public Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) return b;
        }
        return null;
    }
}

package service;

import domain.Book;

import java.time.LocalDate;
import java.util.ArrayList;

public class BookService {
    private final ArrayList<Book> books = new ArrayList<>();

    public void addBook(String title, String author, String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) {
                System.out.println("Book with this ISBN already exists: " + isbn);
                return;
            }
        }
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        System.out.println("Book added successfully: " + newBook);
    }

    public void searchBook(String key) {
        boolean found = false;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(key.toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(key.toLowerCase()) ||
                    b.getIsbn().contains(key)) {
                System.out.println("Match found: " + b);
                found = true;
            }
        }
        if(!found) {
            System.out.println("No book found with this key: " + key);
        }
    }

    public void showAllBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in the library yet.");
        } else {
            System.out.println("All Books:");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }

    // ---------------- Sprint 2 methods ----------------

    public void borrowBook(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) {
            System.out.println("Book not found: " + isbn);
            return;
        }
        try {
            book.borrowBook();
            System.out.println("Book borrowed successfully: " + book.getTitle() + " Due: " + book.getDueDate());
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    public void returnBook(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) {
            System.out.println("Book not found: " + isbn);
            return;
        }
        book.returnBook();
        System.out.println("Book returned successfully: " + book.getTitle());
    }

    public boolean isBookOverdue(String isbn) {
        Book book = findBookByISBN(isbn);
        if (book == null) return false;
        return book.isOverdue();
    }

     Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) return b;
        }
        return null;
    }
}

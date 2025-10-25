package service;

import domain.Book;

import java.util.ArrayList;

public class BookService {

    private final ArrayList<Book> books = new ArrayList<>();

    public void addBook(String title, String author, String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) {
                System.out.println(" Book with this ISBN : " + isbn + " already exists!");
                return;
            }
        }
            Book newBook = new Book(title, author, isbn);
            books.add(newBook);
            System.out.println(" Book added successfully : " + newBook);
        }

    public void searchBook(String key) {
        boolean found = false;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(key.toLowerCase()) ||
                    b.getAuthor().toLowerCase().contains(key.toLowerCase()) ||
                    b.getIsbn().contains(key))
            {
                    System.out.println(" Match found : " + b);
                    found = true;
            }
        }
        if(!found) {
            System.out.println(" No book found with this key : " + key);
        }
    }

    public void showAllBooks() {
        if (books.isEmpty()) {
            System.out.println(" No books in the library yet.");
        }
        else {
            System.out.println(" All Books : ");
            for (Book b : books) {
                System.out.println(b);
            }
        }
    }
}



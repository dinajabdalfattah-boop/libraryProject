package service;

import domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {

    private BookService bookService;

    // dynamic test data
    private String title1;
    private String author1;
    private String isbn1;

    private String title2;
    private String author2;
    private String isbn2;

    @BeforeEach
    public void setUp() {
        bookService = new BookService();

        title1 = "TitleA";
        author1 = "AuthorA";
        isbn1 = "1111";

        title2 = "TitleB";
        author2 = "AuthorB";
        isbn2 = "2222";
    }

    @Test
    public void testAddBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        Book b = bookService.findBookByISBN(isbn1);
        assertNotNull(b);
        assertEquals(title1, b.getTitle());
    }

    @Test
    public void testAddBookDuplicateISBN() {
        bookService.addBook(title1, author1, isbn1);
        bookService.addBook(title2, author2, isbn1);

        assertNotNull(bookService.findBookByISBN(isbn1));
        assertNull(bookService.findBookByISBN(isbn2));
    }

    @Test
    public void testBorrowBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        bookService.borrowBook(isbn1);

        Book b = bookService.findBookByISBN(isbn1);
        assertFalse(b.isAvailable());
        assertNotNull(b.getDueDate());
    }

    @Test
    public void testBorrowBookNotFound() {
        assertDoesNotThrow(() -> bookService.borrowBook("9999"));
    }

    @Test
    public void testBorrowBookAlreadyBorrowed() {
        bookService.addBook(title1, author1, isbn1);

        bookService.borrowBook(isbn1);
        Book b = bookService.findBookByISBN(isbn1);
        assertTrue(b.isBorrowed());

        assertDoesNotThrow(() -> bookService.borrowBook(isbn1));
        assertTrue(b.isBorrowed());
    }

    @Test
    public void testReturnBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        bookService.borrowBook(isbn1);
        bookService.returnBook(isbn1);

        Book b = bookService.findBookByISBN(isbn1);
        assertTrue(b.isAvailable());
        assertNull(b.getDueDate());
    }

    @Test
    public void testReturnBookNotFound() {
        assertDoesNotThrow(() -> bookService.returnBook("9999"));
    }

    @Test
    public void testIsBookOverdueTrue() {
        bookService.addBook(title1, author1, isbn1);
        Book b = bookService.findBookByISBN(isbn1);

        LocalDate past = LocalDate.now().minusDays(40);
        b.borrowBook(past);

        assertTrue(bookService.isBookOverdue(isbn1));
    }

    @Test
    public void testIsBookOverdueFalse() {
        bookService.addBook(title1, author1, isbn1);
        bookService.borrowBook(isbn1);

        assertFalse(bookService.isBookOverdue(isbn1));
    }

    @Test
    public void testIsBookOverdueBookNotFound() {
        assertFalse(bookService.isBookOverdue("9999"));
    }

    @Test
    public void testSearchBookFound() {
        bookService.addBook(title1, author1, isbn1);
        assertDoesNotThrow(() -> bookService.searchBook(title1));
        assertDoesNotThrow(() -> bookService.searchBook(author1));
        assertDoesNotThrow(() -> bookService.searchBook(isbn1));
    }

    @Test
    public void testSearchBookNotFound() {
        assertDoesNotThrow(() -> bookService.searchBook("UNKNOWN"));
    }
}

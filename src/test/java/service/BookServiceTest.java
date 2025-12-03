package service;

import domain.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {

    private BookService bookService;

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

    // =====================
    // ADD BOOK
    // =====================

    @Test
    public void testAddBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        Book b = bookService.findBookByISBN(isbn1);

        assertNotNull(b);
        assertEquals(title1, b.getTitle());
        assertEquals(author1, b.getAuthor());
        assertEquals(isbn1, b.getIsbn());
    }

    @Test
    public void testAddBookDuplicateISBN() {
        bookService.addBook(title1, author1, isbn1);
        bookService.addBook(title2, author2, isbn1);   // نفس الـ ISBN

        assertNotNull(bookService.findBookByISBN(isbn1));
        assertNull(bookService.findBookByISBN(isbn2)); // ما ينضاف الثاني
    }

    /** لتغطية فروع التحقق من المدخلات لو موجودة */
    @Test
    public void testAddBookWithInvalidData() {
        assertDoesNotThrow(() -> bookService.addBook(null, author1, isbn1));
        assertDoesNotThrow(() -> bookService.addBook(title1, null, isbn2));
        assertDoesNotThrow(() -> bookService.addBook(title2, author2, null));
    }

    // =====================
    // BORROW
    // =====================

    @Test
    public void testBorrowBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        bookService.borrowBook(isbn1);

        Book b = bookService.findBookByISBN(isbn1);

        assertFalse(b.isAvailable());
        assertTrue(b.isBorrowed());
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

        // محاولة استعارة ثانية لنفس الكتاب
        assertDoesNotThrow(() -> bookService.borrowBook(isbn1));
        assertTrue(b.isBorrowed());
    }

    // =====================
    // RETURN
    // =====================

    @Test
    public void testReturnBookSuccess() {
        bookService.addBook(title1, author1, isbn1);
        bookService.borrowBook(isbn1);

        bookService.returnBook(isbn1);

        Book b = bookService.findBookByISBN(isbn1);

        assertTrue(b.isAvailable());
        assertFalse(b.isBorrowed());
        assertNull(b.getDueDate());
    }

    @Test
    public void testReturnBookNotFound() {
        assertDoesNotThrow(() -> bookService.returnBook("9999"));
    }

    /** كتاب موجود لكنه مش مستعار → يغطي فرع if (!isBorrowed) في returnBook */
    @Test
    public void testReturnBookNotBorrowedButExists() {
        bookService.addBook(title1, author1, isbn1);
        Book b = bookService.findBookByISBN(isbn1);

        assertTrue(b.isAvailable());
        assertFalse(b.isBorrowed());

        assertDoesNotThrow(() -> bookService.returnBook(isbn1));

        assertTrue(b.isAvailable());
        assertFalse(b.isBorrowed());
        assertNull(b.getDueDate());
    }

    // =====================
    // OVERDUE
    // =====================

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
    public void testLoadBooksFromFile() {
        // نجهز محتوى ملف الكتب كما يتوقع BookService
        List<String> lines = new ArrayList<>();

        // سطر فاضي يغطي line.isBlank()
        lines.add("");

        // كتاب متاح available = true ومع null dates
        lines.add("T1,A1,111,true,null,null");

        // كتاب غير متاح available = false ومع تواريخ حقيقية (الشرطين يتحققوا)
        LocalDate borrow1 = LocalDate.of(2024, 1, 1);
        LocalDate due1    = LocalDate.of(2024, 1, 10);
        lines.add("T2,A2,222,false," + borrow1 + "," + due1);

        // كتاب غير متاح available = false لكن borrowDate = "null" و dueDate حقيقي
        LocalDate due2 = LocalDate.of(2024, 2, 1);
        lines.add("T3,A3,333,false,null," + due2);

        // نكتب الملف في نفس المسار اللي يستخدمه BookService
        FileManager.writeLines("src/main/resources/data/books.txt", lines);

        // نستدعي الميثود اللي عليها اللون الأحمر
        bookService.loadBooksFromFile();

        List<Book> all = bookService.getAllBooks();
        assertEquals(3, all.size());

        // نبحث عن كل كتاب حسب الـ ISBN
        Book book1 = bookService.findBookByISBN("111");
        Book book2 = bookService.findBookByISBN("222");
        Book book3 = bookService.findBookByISBN("333");

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(book3);

        // T1: لازم يكون متاح، بدون تواريخ
        assertTrue(book1.isAvailable());
        assertNull(book1.getBorrowDate());
        assertNull(book1.getDueDate());

        // T2: غير متاح، ومع تواريخ borrow & due
        assertFalse(book2.isAvailable());
        assertEquals(borrow1, book2.getBorrowDate());
        assertEquals(due1, book2.getDueDate());

        // T3: غير متاح، borrowDate = null، dueDate فقط
        assertFalse(book3.isAvailable());
        assertNull(book3.getBorrowDate());
        assertEquals(due2, book3.getDueDate());
    }

    /** كتاب موجود لكن غير مستعار → يغطي فرع !isBorrowed() */
    @Test
    public void testIsBookOverdueBookExistsNotBorrowed() {
        bookService.addBook(title1, author1, isbn1);
        Book b = bookService.findBookByISBN(isbn1);

        assertNotNull(b);
        assertFalse(b.isBorrowed());
        assertFalse(bookService.isBookOverdue(isbn1));
    }

    // =====================
    // SEARCH
    // =====================

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

    /** يغطي فرع query.isEmpty() إن وجد */
    @Test
    public void testSearchBookWithEmptyString() {
        bookService.addBook(title1, author1, isbn1);
        assertDoesNotThrow(() -> bookService.searchBook(""));
    }

    /** يغطي فرع query == null إن وجد */
    @Test
    public void testSearchBookWithNull() {
        bookService.addBook(title1, author1, isbn1);
        assertDoesNotThrow(() -> bookService.searchBook(null));
    }

    // =====================
    // GET ALL BOOKS (لو الميثود موجودة)
    // =====================

    @Test
    public void testGetAllBooksEmpty() {
        List<Book> all = bookService.getAllBooks();
        assertNotNull(all);
        assertTrue(all.isEmpty());
    }

    @Test
    public void testGetAllBooksNonEmpty() {
        bookService.addBook(title1, author1, isbn1);
        bookService.addBook(title2, author2, isbn2);

        List<Book> all = bookService.getAllBooks();
        assertEquals(2, all.size());
    }
}

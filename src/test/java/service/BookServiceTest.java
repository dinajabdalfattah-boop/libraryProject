package service;

import domain.Book;
import file.FileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookServiceTest {

    private BookService bookService;
    private static final String FILE = "src/main/resources/data/books.txt";

    @BeforeEach
    public void setUp() {
        bookService = new BookService();
        FileManager.writeLines(FILE, new ArrayList<>());
    }

    // ---------------------------------------------------------
    // ADD BOOK
    // ---------------------------------------------------------

    @Test
    public void testAddBookSuccess() {
        assertTrue(bookService.addBook("T1", "A1", "111"));

        Book b = bookService.findBookByISBN("111");
        assertNotNull(b);
        assertEquals("T1", b.getTitle());
    }

    @Test
    public void testAddBookDuplicateISBN() {
        assertTrue(bookService.addBook("A", "A", "111"));
        assertFalse(bookService.addBook("B", "B", "111"));
    }

    @Test
    public void testAddBookNullValuesAllowed() {
        assertTrue(bookService.addBook(null, null, "999"));
        assertNotNull(bookService.findBookByISBN("999"));
    }

    // ---------------------------------------------------------
    // LOAD FROM FILE — MAIN VALID CASES
    // ---------------------------------------------------------

    @Test
    public void testLoadBooksFromFileValid() {

        List<String> lines = new ArrayList<>();

        lines.add(""); // blank → skipped
        lines.add("T1,A1,111,true,null,null");

        LocalDate bd = LocalDate.of(2024, 1, 1);
        LocalDate dd = LocalDate.of(2024, 1, 10);
        lines.add("T2,A2,222,false," + bd + "," + dd);

        LocalDate dueOnly = LocalDate.of(2024, 2, 1);
        lines.add("T3,A3,333,false,null," + dueOnly);

        FileManager.writeLines(FILE, lines);

        bookService.loadBooksFromFile();
        List<Book> all = bookService.getAllBooks();

        assertEquals(3, all.size());
    }

    // ---------------------------------------------------------
    // LOAD FROM FILE — INVALID FIELDS / BRANCHES
    // ---------------------------------------------------------

    @Test
    public void testLoadBooksFromFileInvalidLine() {
        FileManager.writeLines(FILE, List.of("invalid_with_no_commas"));
        assertDoesNotThrow(() -> bookService.loadBooksFromFile());
        assertEquals(0, bookService.getAllBooks().size());
    }

    @Test
    public void testLoadBooksFromFileMissingFields() {
        FileManager.writeLines(FILE, List.of("T1,A1")); // length < 6
        assertDoesNotThrow(() -> bookService.loadBooksFromFile());
        assertEquals(0, bookService.getAllBooks().size());
    }

    @Test
    public void testLoadBooksFromFileAvailableTrueIgnoresDates() {
        FileManager.writeLines(FILE,
                List.of("T1,A1,111,true,2024-01-01,2024-02-01"));

        bookService.loadBooksFromFile();
        Book b = bookService.findBookByISBN("111");

        assertNotNull(b);
        assertTrue(b.isAvailable());
        assertNull(b.getBorrowDate());
        assertNull(b.getDueDate());
    }

    @Test
    public void testLoadBooksFromFileAvailableNullTreatedAsFalse() {

        FileManager.writeLines(FILE,
                List.of("T1,A1,111,null,2024-01-01,2024-02-01"));

        bookService.loadBooksFromFile();

        Book b = bookService.findBookByISBN("111");

        assertNotNull(b);
        assertFalse(b.isAvailable()); // "null" → false
        assertEquals(LocalDate.parse("2024-01-01"), b.getBorrowDate());
        assertEquals(LocalDate.parse("2024-02-01"), b.getDueDate());
    }

    // ---------------------------------------------------------
    // SEARCH TESTS
    // ---------------------------------------------------------

    @Test
    public void testSearchFindsResults() {
        bookService.addBook("Java Programming", "Mark", "111");
        bookService.addBook("Python Guide", "Anna", "222");

        assertEquals(1, bookService.search("java").size());
        assertEquals(1, bookService.search("anna").size());
        assertEquals(1, bookService.search("222").size());
    }

    @Test
    public void testSearchNoMatches() {
        bookService.addBook("Java", "A", "111");
        assertTrue(bookService.search("xyz").isEmpty());
    }

    @Test
    public void testSearchCaseInsensitive() {
        bookService.addBook("JAVA BOOK", "AUTHOR", "ID1");
        assertEquals(1, bookService.search("java").size());
    }

    @Test
    public void testSearchEmptyKeyword() {
        bookService.addBook("A", "B", "111");
        assertEquals(1, bookService.search("").size());
    }

    @Test
    public void testSearchSpacesKeyword() {
        bookService.addBook("A Book", "B", "111");
        assertEquals(1, bookService.search("   ").size());
    }

    @Test
    public void testSearchNullKeywordThrows() {
        assertThrows(NullPointerException.class, () -> bookService.search(null));
    }

    // ---------------------------------------------------------
    // FIND BY ISBN
    // ---------------------------------------------------------

    @Test
    public void testFindBookByIsbnNotFound() {
        assertNull(bookService.findBookByISBN("999"));
    }

    @Test
    public void testFindBookByIsbnFound() {
        bookService.addBook("T", "A", "111");
        assertNotNull(bookService.findBookByISBN("111"));
    }

    // ---------------------------------------------------------
    // GET ALL BOOKS
    // ---------------------------------------------------------

    @Test
    public void testGetAllBooksEmpty() {
        assertTrue(bookService.getAllBooks().isEmpty());
    }

    @Test
    public void testGetAllBooksNotEmpty() {
        bookService.addBook("A", "A", "1");
        assertEquals(1, bookService.getAllBooks().size());
    }
}

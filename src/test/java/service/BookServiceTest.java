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

    @Test
    public void addBook_success_and_saved() {
        assertTrue(bookService.addBook("Clean Code", "Robert Martin", "111"));
        Book b = bookService.findBookByISBN("111");
        assertNotNull(b);
        assertEquals("Clean Code", b.getTitle());
        assertEquals("Robert Martin", b.getAuthor());

        List<String> lines = FileManager.readLines(FILE);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).startsWith("Clean Code,Robert Martin,111,"));
    }

    @Test
    public void addBook_duplicate_isbn_false() {
        assertTrue(bookService.addBook("A", "B", "111"));
        assertFalse(bookService.addBook("X", "Y", "111"));
        assertEquals(1, bookService.getAllBooks().size());
    }

    @Test
    public void addBook_allows_null_title_author() {
        assertTrue(bookService.addBook(null, null, "999"));
        assertNotNull(bookService.findBookByISBN("999"));
    }

    @Test
    public void saveBooksToFile_writes_null_and_real_dates() {
        bookService.addBook("B1", "A1", "1");
        bookService.addBook("B2", "A2", "2");

        Book b2 = bookService.findBookByISBN("2");

        LocalDate bd = LocalDate.of(2025, 11, 17);
        LocalDate dd = LocalDate.of(2025, 12, 15);

        b2.setAvailable(false);
        b2.setBorrowDate(bd);
        b2.setDueDate(dd);

        bookService.saveBooksToFile();

        List<String> lines = FileManager.readLines(FILE);
        assertEquals(2, lines.size());

        String l1 = lines.stream().filter(s -> s.contains(",1,")).findFirst().orElse("");
        String l2 = lines.stream().filter(s -> s.contains(",2,")).findFirst().orElse("");

        assertTrue(l1.endsWith(",null,null"));
        assertTrue(l2.endsWith("," + bd + "," + dd));
    }

    @Test
    public void loadBooksFromFile_skips_invalid_and_loads_valid() {
        List<String> lines = new ArrayList<>();
        lines.add("");
        lines.add("invalid");
        lines.add("T1,A1");
        lines.add("T2,A2,222,true,null,null");
        FileManager.writeLines(FILE, lines);

        bookService.loadBooksFromFile();

        assertEquals(1, bookService.getAllBooks().size());
        assertNotNull(bookService.findBookByISBN("222"));
    }

    @Test
    public void loadBooksFromFile_covers_all_branches() {
        List<String> lines = new ArrayList<>();

        lines.add("B1,A1,111");
        lines.add("B2,A2,222,null,2025-11-17,2025-12-15");
        lines.add("B3,A3,333,   ,2025-11-17,2025-12-15");
        lines.add("B4,A4,444,true, ,null");
        lines.add("B5,A5,555,false,2025-11-17, ");
        lines.add("B6,A6,666,false,null,2025-12-15");

        FileManager.writeLines(FILE, lines);
        bookService.loadBooksFromFile();

        Book b1 = bookService.findBookByISBN("111");
        Book b2 = bookService.findBookByISBN("222");
        Book b3 = bookService.findBookByISBN("333");
        Book b4 = bookService.findBookByISBN("444");
        Book b5 = bookService.findBookByISBN("555");
        Book b6 = bookService.findBookByISBN("666");

        assertFalse(b1.isAvailable());
        assertNull(b1.getBorrowDate());
        assertNull(b1.getDueDate());

        assertFalse(b2.isAvailable());
        assertEquals(LocalDate.parse("2025-11-17"), b2.getBorrowDate());
        assertEquals(LocalDate.parse("2025-12-15"), b2.getDueDate());

        assertFalse(b3.isAvailable());
        assertEquals(LocalDate.parse("2025-11-17"), b3.getBorrowDate());
        assertEquals(LocalDate.parse("2025-12-15"), b3.getDueDate());

        assertTrue(b4.isAvailable());
        assertNull(b4.getBorrowDate());
        assertNull(b4.getDueDate());

        assertFalse(b5.isAvailable());
        assertEquals(LocalDate.parse("2025-11-17"), b5.getBorrowDate());
        assertNull(b5.getDueDate());

        assertFalse(b6.isAvailable());
        assertNull(b6.getBorrowDate());
        assertEquals(LocalDate.parse("2025-12-15"), b6.getDueDate());
    }

    @Test
    public void loadBooksFromFile_invalid_date_throws() {
        FileManager.writeLines(FILE, List.of("T,A,111,false,not-a-date,2025-12-15"));
        assertThrows(Exception.class, () -> bookService.loadBooksFromFile());
    }

    @Test
    public void search_null_throws() {
        assertThrows(NullPointerException.class, () -> bookService.search(null));
    }

    @Test
    public void search_blank_returns_all() {
        bookService.addBook("Java", "Mark", "111");
        bookService.addBook("Python", "Anna", "222");
        assertEquals(2, bookService.search("").size());
        assertEquals(2, bookService.search("   ").size());
    }

    @Test
    public void search_matches_title_author_isbn_and_no_match() {
        bookService.addBook("Java Programming", "Mark", "111");
        bookService.addBook("Data Structures", "ANNA", "222");
        bookService.addBook("Networks", "Omar", "333");

        assertEquals(1, bookService.search("java").size());
        assertEquals(1, bookService.search("anna").size());
        assertEquals(1, bookService.search("222").size());
        assertTrue(bookService.search("zzz").isEmpty());
    }

    @Test
    public void findBookByISBN_found_and_not_found() {
        assertNull(bookService.findBookByISBN("nope"));
        bookService.addBook("T", "A", "1");
        assertNotNull(bookService.findBookByISBN("1"));
    }

    @Test
    public void getAllBooks_reflects_state() {
        assertTrue(bookService.getAllBooks().isEmpty());
        bookService.addBook("T", "A", "1");
        assertEquals(1, bookService.getAllBooks().size());
    }
}

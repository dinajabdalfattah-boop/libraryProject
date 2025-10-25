package domain;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class bookTest {

    private Book book;

    @BeforeEach
    public void setUp() {
        book = new Book("Clean Code", "Robert Martin", "978-0132350884");
    }

    @Test
    public void testGetters() {
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor());
        assertEquals("978-0132350884", book.getIsbn());
        assertTrue(book.isAvailable()); // المفروض الكتاب متاح بشكل افتراضي
    }

    @Test
    public void testSetAvailable() {
        book.setAvailable(false);
        assertFalse(book.isAvailable());

        book.setAvailable(true);
        assertTrue(book.isAvailable());
    }

    @Test
    public void testToStringContainsAllInfo() {
        String result = book.toString();

        assertTrue(result.contains("Clean Code"));
        assertTrue(result.contains("Robert Martin"));
        assertTrue(result.contains("978-0132350884"));
        assertTrue(result.contains("Available")); // افتراضيًا متاح
    }

    @Test
    public void testToStringWhenNotAvailable() {
        book.setAvailable(false);
        String result = book.toString();
        assertTrue(result.contains("Not Available"));
    }
}

package Domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibraryTest {
    @Test
    void testBookToString() {
        Book book = new Book("Java", "Author", "12345");
        assertTrue(book.toString().contains("Java"));
    }
}

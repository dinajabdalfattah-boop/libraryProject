package domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CDTest {

    private CD cd;

    @BeforeEach
    public void setup() {
        cd = new CD("MyCD", "ArtistX", "CD100");
    }

    // ---------------------------------------------------------
    // Constructor + Basic State
    // ---------------------------------------------------------

    @Test
    public void testConstructorAndGetters() {
        assertEquals("MyCD", cd.getTitle());
        assertEquals("ArtistX", cd.getArtist());
        assertEquals("CD100", cd.getId());

        assertTrue(cd.isAvailable());
        assertNull(cd.getBorrowDate());
        assertNull(cd.getDueDate());
    }

    // ---------------------------------------------------------
    // Borrowing Tests
    // ---------------------------------------------------------

    @Test
    public void testBorrowCDWithDate() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        cd.borrowCD(date);

        assertFalse(cd.isAvailable());
        assertEquals(date, cd.getBorrowDate());
        assertEquals(date.plusDays(7), cd.getDueDate());
    }

    @Test
    public void testBorrowCDWithoutDate() {
        cd.borrowCD();

        assertFalse(cd.isAvailable());
        assertNotNull(cd.getBorrowDate());
        assertNotNull(cd.getDueDate());
    }

    @Test
    public void testBorrowCDThrowsWhenAlreadyBorrowed() {
        cd.borrowCD(LocalDate.now());

        assertThrows(IllegalStateException.class, () -> cd.borrowCD(LocalDate.now()));
    }

    // ---------------------------------------------------------
    // Return Tests
    // ---------------------------------------------------------

    @Test
    public void testReturnCD() {
        cd.borrowCD(LocalDate.now());
        cd.returnCD();

        assertTrue(cd.isAvailable());
        assertNull(cd.getBorrowDate());
        assertNull(cd.getDueDate());
    }

    @Test
    public void testReturnCDWhenAlreadyAvailable() {
        // Should NOT throw, should reset safely
        cd.returnCD();
        assertTrue(cd.isAvailable());
        assertNull(cd.getBorrowDate());
        assertNull(cd.getDueDate());
    }

    // ---------------------------------------------------------
    // Overdue Tests
    // ---------------------------------------------------------

    @Test
    public void testIsOverdueTrue() {
        cd.borrowCD(LocalDate.now().minusDays(10));
        assertTrue(cd.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueFalse() {
        cd.borrowCD(LocalDate.now());
        assertFalse(cd.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueWhenDueDateNull() {
        assertFalse(cd.isOverdue(LocalDate.now()));
    }

    @Test
    public void testIsOverdueDefaultMethod() {
        cd.borrowCD(LocalDate.now().minusDays(10));
        assertTrue(cd.isOverdue());
    }

    // ---------------------------------------------------------
    // Remaining Days
    // ---------------------------------------------------------

    @Test
    public void testRemainingDaysWhenDueDateNull() {
        assertEquals(0, cd.getRemainingDays(LocalDate.now()));
    }

    @Test
    public void testRemainingDaysPositive() {
        LocalDate date = LocalDate.now();
        cd.borrowCD(date);

        assertEquals(7, cd.getRemainingDays(date));
    }

    @Test
    public void testRemainingDaysNegative() {
        cd.borrowCD(LocalDate.now().minusDays(10));

        int days = cd.getRemainingDays(LocalDate.now());
        assertTrue(days < 0);
    }

    // ---------------------------------------------------------
    // toString Tests
    // ---------------------------------------------------------

    @Test
    public void testToStringAvailable() {
        String s = cd.toString();

        assertTrue(s.contains("MyCD"));
        assertTrue(s.contains("ArtistX"));
        assertTrue(s.contains("CD100"));
        assertTrue(s.contains("Available=true"));
    }

    @Test
    public void testToStringBorrowed() {
        LocalDate date = LocalDate.now();
        cd.borrowCD(date);

        String s = cd.toString();
        assertTrue(s.contains("MyCD"));
        assertTrue(s.contains("ArtistX"));
        assertTrue(s.contains("Available=false"));
        assertTrue(s.contains(cd.getDueDate().toString()));
    }
}

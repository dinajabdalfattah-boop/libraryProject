package domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class CDTest {

    @Test
    void testInitialState() {
        CD cd = new CD("Album", "Artist", "CD1");

        assertTrue(cd.isAvailable());
        assertNull(cd.getBorrowDate());
        assertNull(cd.getDueDate());
        assertEquals("Album", cd.getTitle());
        assertEquals("Artist", cd.getArtist());
        assertEquals("CD1", cd.getId());
    }

    @Test
    void testBorrowCDWithDate() {
        CD cd = new CD("A", "B", "1");
        LocalDate date = LocalDate.of(2025, 1, 1);

        cd.borrowCD(date);

        assertFalse(cd.isAvailable());
        assertEquals(date, cd.getBorrowDate());
        assertEquals(date.plusDays(7), cd.getDueDate());
    }

    @Test
    void testBorrowCDToday() {
        CD cd = new CD("A", "B", "1");

        cd.borrowCD();

        assertFalse(cd.isAvailable());
        assertNotNull(cd.getBorrowDate());
        assertNotNull(cd.getDueDate());
    }

    @Test
    void testBorrowAlreadyBorrowedThrows() {
        CD cd = new CD("A", "B", "1");
        cd.borrowCD(LocalDate.now());

        assertThrows(IllegalStateException.class,
                () -> cd.borrowCD(LocalDate.now().plusDays(1)));
    }

    @Test
    void testReturnCD() {
        CD cd = new CD("A", "B", "1");
        cd.borrowCD(LocalDate.now());

        cd.returnCD();

        assertTrue(cd.isAvailable());
        assertNull(cd.getBorrowDate());
        assertNull(cd.getDueDate());
    }

    @Test
    void testOverdueTrue() {
        CD cd = new CD("A", "B", "1");
        cd.borrowCD(LocalDate.now().minusDays(10));

        assertTrue(cd.isOverdue());
    }

    @Test
    void testOverdueFalse() {
        CD cd = new CD("A", "B", "1");
        cd.borrowCD(LocalDate.now());

        assertFalse(cd.isOverdue());
    }

    @Test
    void testRemainingDaysPositive() {
        CD cd = new CD("A", "B", "1");
        cd.borrowCD(LocalDate.now());

        assertTrue(cd.getRemainingDays(LocalDate.now()) >= 0);
    }

    @Test
    void testRemainingDaysZeroWhenNotBorrowed() {
        CD cd = new CD("A", "B", "1");

        assertEquals(0, cd.getRemainingDays(LocalDate.now()));
    }

    @Test
    void testSetBorrowAndDueDates() {
        CD cd = new CD("A", "B", "1");

        LocalDate b = LocalDate.of(2025,1,1);
        LocalDate d = LocalDate.of(2025,1,8);

        cd.setBorrowDate(b);
        cd.setDueDate(d);

        assertEquals(b, cd.getBorrowDate());
        assertEquals(d, cd.getDueDate());
    }
}

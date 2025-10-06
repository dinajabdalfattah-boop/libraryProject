import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class libraryTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void add() {

            library calc = new library();
            int result = calc.add(2, 3);
            assertEquals(5, result, "Addition should be correct");
    }
    @Test
    void testMuliply() {

        library mult = new library();
        int r = mult.multiply(3, 3);
        assertEquals(9, r, "Multiplication should be correct");
    }


}
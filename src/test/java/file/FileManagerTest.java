package file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileManagerTest {

    private final String BASE_DIR = "src/test/resources/tmp_fm/";

    @BeforeEach
    public void reset() throws Exception {
        File base = new File(BASE_DIR);
        if (base.exists()) {
            deleteRecursive(base);
        }
        base.mkdirs();
    }

    private void deleteRecursive(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                deleteRecursive(c);
            }
        }
        f.delete();
    }

    // ---------------------------------------------------------
    // readLines() tests
    // ---------------------------------------------------------

    @Test
    public void testReadLinesFileDoesNotExistCreatesEmptyList() {
        String path = BASE_DIR + "newFile.txt";

        List<String> lines = FileManager.readLines(path);

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
        assertTrue(new File(path).exists());
    }

    @Test
    public void testReadLinesIOExceptionCatchesError() throws Exception {
        String path = BASE_DIR + "blocked";

        // Create a directory instead of file → readAllLines() will throw IOException
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.readLines(path));
    }

    // ---------------------------------------------------------
    // writeLines() tests
    // ---------------------------------------------------------

    @Test
    public void testWriteLinesSuccess() {
        String path = BASE_DIR + "write.txt";
        List<String> data = List.of("A", "B", "C");

        FileManager.writeLines(path, data);

        List<String> read = FileManager.readLines(path);
        assertEquals(3, read.size());
        assertEquals("A", read.get(0));
        assertEquals("C", read.get(2));
    }

    @Test
    public void testWriteLinesIOExceptionHandled() {
        String path = BASE_DIR + "cantWrite";

        // make directory instead of file to force IOException
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.writeLines(path, List.of("X")));
    }

    // ---------------------------------------------------------
    // appendLine() tests
    // ---------------------------------------------------------

    @Test
    public void testAppendLineSuccess() {
        String path = BASE_DIR + "append.txt";

        FileManager.writeLines(path, new ArrayList<>());
        FileManager.appendLine(path, "Hello");

        List<String> read = FileManager.readLines(path);

        assertEquals(1, read.size());
        assertEquals("Hello", read.get(0).trim());
    }

    @Test
    public void testAppendLineIOExceptionHandled() {
        String path = BASE_DIR + "appendBlocked";

        // make directory → append will fail
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.appendLine(path, "TEST"));
    }
}

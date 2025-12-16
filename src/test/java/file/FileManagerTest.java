package file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
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
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) {
                    deleteRecursive(c);
                }
            }
        }
        boolean deleted = f.delete();
        if (!deleted && f.exists()) {
            fail("Failed to delete file: " + f.getAbsolutePath());
        }
    }

    @Test
    public void readLines_createsParentDirsAndFile_whenMissing() {
        String path = BASE_DIR + "a/b/c/new.txt";

        List<String> lines = FileManager.readLines(path);

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
        assertTrue(new File(path).exists());
        assertTrue(new File(BASE_DIR + "a/b/c").exists());
    }

    @Test
    public void readLines_existingFile_returnsAllLines() {
        String path = BASE_DIR + "read.txt";
        FileManager.writeLines(path, List.of("L1", "L2", "L3"));

        List<String> lines = FileManager.readLines(path);

        assertEquals(3, lines.size());
        assertEquals("L1", lines.get(0));
        assertEquals("L3", lines.get(2));
    }

    @Test
    public void readLines_existingEmptyFile_returnsEmptyList() {
        String path = BASE_DIR + "empty.txt";
        FileManager.writeLines(path, new ArrayList<>());

        List<String> lines = FileManager.readLines(path);

        assertNotNull(lines);
        assertTrue(lines.isEmpty());
    }

    @Test
    public void readLines_throwsRuntimeException_whenPathIsDirectory() {
        String path = BASE_DIR + "dirAsFile";
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.readLines(path));
    }

    @Test
    public void writeLines_createsParentDirsAndFile_whenMissing() {
        String path = BASE_DIR + "x/y/z/write.txt";

        FileManager.writeLines(path, List.of("A", "B"));

        assertTrue(new File(path).exists());
        assertEquals(List.of("A", "B"), FileManager.readLines(path));
    }

    @Test
    public void writeLines_replacesExistingContent_notAppend() {
        String path = BASE_DIR + "replace.txt";
        FileManager.writeLines(path, List.of("1", "2", "3"));

        FileManager.writeLines(path, List.of("X"));

        List<String> lines = FileManager.readLines(path);
        assertEquals(1, lines.size());
        assertEquals("X", lines.get(0));
    }

    @Test
    public void writeLines_allowsEmptyList_andClearsFile() {
        String path = BASE_DIR + "clear.txt";
        FileManager.writeLines(path, List.of("A", "B"));

        FileManager.writeLines(path, new ArrayList<>());

        List<String> lines = FileManager.readLines(path);
        assertTrue(lines.isEmpty());
    }

    @Test
    public void writeLines_throwsRuntimeException_whenPathIsDirectory() {
        String path = BASE_DIR + "cantWrite";
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.writeLines(path, List.of("X")));
    }

    @Test
    public void appendLine_createsFileAutomatically_whenMissing() {
        String path = BASE_DIR + "append/newAppend.txt";

        FileManager.appendLine(path, "Hello");

        List<String> lines = FileManager.readLines(path);
        assertEquals(1, lines.size());
        assertEquals("Hello", lines.get(0));
        assertTrue(new File(path).exists());
    }

    @Test
    public void appendLine_appendsMultipleLines_inOrder() {
        String path = BASE_DIR + "appendMany.txt";

        FileManager.appendLine(path, "A");
        FileManager.appendLine(path, "B");
        FileManager.appendLine(path, "C");

        List<String> lines = FileManager.readLines(path);
        assertEquals(List.of("A", "B", "C"), lines);
    }

    @Test
    public void appendLine_preservesExistingContent() {
        String path = BASE_DIR + "appendPreserve.txt";
        FileManager.writeLines(path, List.of("L1"));

        FileManager.appendLine(path, "L2");

        List<String> lines = FileManager.readLines(path);
        assertEquals(2, lines.size());
        assertEquals("L1", lines.get(0));
        assertEquals("L2", lines.get(1));
    }

    @Test
    public void appendLine_allowsEmptyString_line() {
        String path = BASE_DIR + "appendEmptyLine.txt";

        FileManager.appendLine(path, "");

        List<String> lines = FileManager.readLines(path);
        assertEquals(1, lines.size());
        assertEquals("", lines.get(0));
    }

    @Test
    public void appendLine_throwsRuntimeException_whenPathIsDirectory() {
        String path = BASE_DIR + "appendBlocked";
        new File(path).mkdirs();

        assertThrows(RuntimeException.class, () -> FileManager.appendLine(path, "TEST"));
    }
}

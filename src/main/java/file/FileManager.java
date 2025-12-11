package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling simple file operations such as reading, writing,
 * and appending lines. All methods are static so the class can be used
 * directly without creating objects.
 */
public class FileManager {

    /**
     * Prevents instantiation of this utility class.
     */
    private FileManager() {
        // Utility class: prevent instantiation
    }

    /**
     * Reads all lines from a text file and returns them as a List of strings.
     * If the file does not exist, it will be created automatically and an empty
     * list will be returned.
     *
     * @param filePath the path to the file to read
     * @return a List containing all lines in the file, or an empty list if the file was newly created
     */
    public static List<String> readLines(String filePath) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
                return new ArrayList<>();
            }
            return Files.readAllLines(Paths.get(filePath));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a list of lines into the file, replacing any previous content.
     * If the file or its parent directory does not exist, they will be created.
     *
     * @param filePath the path to the file to write to
     * @param lines    the list of lines to write into the file
     */
    public static void writeLines(String filePath, List<String> lines) {
        try {
            File f = new File(filePath);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            Files.write(Paths.get(filePath), lines);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends a single line to the end of the file.
     * If the file does not exist, it will be created automatically.
     *
     * @param filePath the path to the file
     * @param line     the text line to append
     */
    public static void appendLine(String filePath, String line) {
        try {
            Files.write(
                    Paths.get(filePath),
                    (line + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

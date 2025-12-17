package file;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
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
    }

    /**
     * Represents a missing value in stored CSV files.
     */
    public static final String NULL_LITERAL = "null";

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
                File parent = f.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                boolean created = f.createNewFile();
                if (!created && !f.exists()) {
                    throw new IOException("Failed to create file: " + filePath);
                }

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
                File parent = f.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                boolean created = f.createNewFile();
                if (!created && !f.exists()) {
                    throw new IOException("Failed to create file: " + filePath);
                }
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
            Path path = Paths.get(filePath);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.write(
                    path,
                    (line + System.lineSeparator()).getBytes(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a boolean value from a string in a strict and safe way.
     * Null, blank, or the literal "null" are treated as false.
     *
     * @param s the string to parse
     * @return the parsed boolean value, or false for null/blank/"null"
     */
    public static boolean parseBooleanStrict(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase(NULL_LITERAL)) return false;
        return Boolean.parseBoolean(s);
    }

    /**
     * Parses a LocalDate from a string.
     * Null, blank, or the literal "null" are treated as null.
     *
     * @param s the string to parse
     * @return the parsed LocalDate, or null for null/blank/"null"
     */
    public static LocalDate parseDateOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase(NULL_LITERAL)) return null;
        return LocalDate.parse(s);
    }

    /**
     * Converts a LocalDate into a storable string.
     *
     * @param d the date value (nullable)
     * @return ISO date string, or "null" if the value is null
     */
    public static String dateToStringOrNull(LocalDate d) {
        return d == null ? NULL_LITERAL : d.toString();
    }

    /**
     * Safely returns a part from a split array.
     *
     * @param parts the split array
     * @param index required index
     * @return the element at index if present, otherwise null
     */
    public static String getPart(String[] parts, int index) {
        return parts.length > index ? parts[index] : null;
    }
}

package service;

import domain.CD;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing CDs in the library system.
 * This class supports adding CDs, searching, loading/saving data,
 * and retrieving CDs by their unique ID.
 */
public class CDService {

    private final List<CD> cds = new ArrayList<>();
    private static final String CD_FILE = "src/main/resources/data/cds.txt";

    /**
     * Adds a new CD to the system if the given ID is not already used.
     * The updated list is saved to the storage file after a successful insert.
     *
     * @param title the CD title
     * @param artist the CD artist
     * @param id the unique identifier of the CD
     * @return true if the CD was added successfully, false if a CD with the same ID already exists
     */
    public boolean addCD(String title, String artist, String id) {

        if (findCDById(id) != null)
            return false;

        CD cd = new CD(title, artist, id);
        cds.add(cd);

        saveCDsToFile();
        return true;
    }

    /**
     * Saves all CDs to the storage file using a comma-separated format:
     * title,artist,id,available,borrowDate,dueDate
     * Dates are stored as ISO strings, and missing values are stored as "null".
     */
    public void saveCDsToFile() {

        List<String> lines = new ArrayList<>();

        for (CD c : cds) {
            String line = String.join(",",
                    c.getTitle(),
                    c.getArtist(),
                    c.getId(),
                    String.valueOf(c.isAvailable()),
                    c.getBorrowDate() == null ? "null" : c.getBorrowDate().toString(),
                    c.getDueDate() == null ? "null" : c.getDueDate().toString()
            );
            lines.add(line);
        }

        FileManager.writeLines(CD_FILE, lines);
    }

    /**
     * Parses a boolean value from a string in a strict and safe way.
     * Null, blank, or the literal "null" are treated as false.
     *
     * @param s the string to parse
     * @return the parsed boolean value, or false for null/blank/"null"
     */
    private boolean parseBooleanStrict(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.isEmpty()) return false;
        if (s.equalsIgnoreCase("null")) return false;
        return Boolean.parseBoolean(s);
    }

    /**
     * Parses a LocalDate from a string.
     * Null, blank, or the literal "null" are treated as null.
     *
     * @param s the string to parse
     * @return the parsed LocalDate, or null for null/blank/"null"
     */
    private LocalDate parseDateOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(s);
    }

    /**
     * Loads CDs from the storage file into memory.
     * Invalid or incomplete lines are ignored.
     * If a CD is marked as available, borrow and due dates are cleared.
     */
    public void loadCDsFromFile() {

        cds.clear();

        List<String> lines = FileManager.readLines(CD_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank())
                continue;

            String[] p = line.split(",");

            if (p.length < 3)
                continue;

            CD cd = new CD(p[0], p[1], p[2]);

            String availableStr = (p.length > 3) ? p[3] : null;
            boolean available = parseBooleanStrict(availableStr);

            if (available) {
                cd.returnCD();
            } else {
                String borrowStr = (p.length > 4) ? p[4] : null;
                String dueStr = (p.length > 5) ? p[5] : null;

                cd.setBorrowDate(parseDateOrNull(borrowStr));
                cd.setDueDate(parseDateOrNull(dueStr));

                cd.borrowCD(cd.getBorrowDate());
            }

            cds.add(cd);
        }
    }

    /**
     * Searches for CDs using a keyword.
     * Matching rules:
     * - if keyword is blank, returns all CDs
     * - title contains keyword (case-insensitive)
     * - artist matches keyword (case-insensitive)
     * - ID matches keyword (exact match)
     *
     * @param keyword the search keyword (must not be null)
     * @return a list of matching CDs
     * @throws NullPointerException if keyword is null
     */
    public List<CD> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

        keyword = keyword.trim();
        if (keyword.isEmpty())
            return new ArrayList<>(cds);

        String keyLower = keyword.toLowerCase();

        List<CD> result = new ArrayList<>();

        for (CD c : cds) {

            if (c.getTitle().toLowerCase().contains(keyLower)) {
                result.add(c);
                continue;
            }

            if (c.getArtist().equalsIgnoreCase(keyword)) {
                result.add(c);
                continue;
            }

            if (c.getId().equals(keyword)) {
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Finds a CD by its unique ID.
     *
     * @param id the ID to search for
     * @return the matching CD if found, otherwise null
     */
    public CD findCDById(String id) {
        for (CD c : cds)
            if (c.getId().equals(id))
                return c;
        return null;
    }

    /**
     * Returns all CDs currently loaded in memory.
     *
     * @return the list of all CDs
     */
    public List<CD> getAllCDs() {
        return cds;
    }
}

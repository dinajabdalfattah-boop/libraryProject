package service;

import domain.CD;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing CD items in the library system.
 * Supports:
 * - adding CDs
 * - loading CDs from file
 * - saving CDs to file
 * - searching CDs
 * - finding CDs by ID
 */
public class CDService {

    private final List<CD> cds = new ArrayList<>();
    private static final String CD_FILE = "src/main/resources/data/cds.txt";

    /**
     * Adds a new CD only if ID does not already exist.
     * Automatically saves the updated CD list to file.
     */
    public boolean addCD(String title, String artist, String id) {

        if (findCDById(id) != null)
            return false;

        CD cd = new CD(title, artist, id);
        cds.add(cd);

        saveCDsToFile();   // 🔥 حفظ تلقائي
        return true;
    }

    /**
     * Saves all CDs to the file in this format:
     * title,artist,id,available,borrowDate,dueDate
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
     * Loads CDs from file.
     * Borrow state is always reset — CDLoanService handles restoring active loans.
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

            // Always reset state — active loans will be re-applied by CDLoanService
            cd.returnCD();

            cds.add(cd);
        }
    }

    /**
     * Searches for CDs by keyword (case-insensitive)
     * Checks title, artist, ID.
     */
    public List<CD> search(String keyword) {

        if (keyword == null)
            throw new NullPointerException("keyword is null");

        if (keyword.trim().isEmpty())
            return new ArrayList<>(cds);

        keyword = keyword.toLowerCase();
        List<CD> result = new ArrayList<>();

        for (CD c : cds) {
            if (c.getTitle().toLowerCase().contains(keyword) ||
                    c.getArtist().toLowerCase().contains(keyword) ||
                    c.getId().toLowerCase().contains(keyword)) {

                result.add(c);
            }
        }

        return result;
    }

    /**
     * Finds a CD by its ID.
     */
    public CD findCDById(String id) {
        for (CD c : cds)
            if (c.getId().equals(id))
                return c;
        return null;
    }

    /**
     * Returns all CDs currently stored in memory.
     */
    public List<CD> getAllCDs() {
        return cds;
    }
}

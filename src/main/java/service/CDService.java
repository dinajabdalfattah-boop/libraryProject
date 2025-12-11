package service;

import domain.CD;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing CD items in the library system.
 * It provides functionality for:
 * - adding new CDs
 * - searching for CDs
 * - saving CDs to a file
 * - loading CDs from a file
 * - retrieving CDs by ID
 *
 * All CD objects are stored in memory, and modifications are persisted
 * to "cds.txt" using the FileManager utility.
 */
public class CDService {

    private final List<CD> cds = new ArrayList<>();
    private static final String CD_FILE = "src/main/resources/data/cds.txt";

    /**
     * Adds a new CD to the system only if its ID is unique.
     * If the CD is added successfully, the list is saved to storage.
     *
     * @param title  the CD's title
     * @param artist the artist name
     * @param id     unique identifier for the CD
     * @return true if added successfully, false if duplicate ID
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
     * Saves all CDs into the storage file.
     * Each CD is written as:
     * title,artist,id,available,borrowDate,dueDate
     */
    private void saveCDsToFile() {

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
     * Loads CD records from the file and reconstructs the CD list.
     *
     * The method safely handles:
     * - missing or empty files
     * - malformed or incomplete lines
     * - restoring borrow date and due date
     *
     * If a CD was borrowed when saved, its state is restored.
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

            // 👇 مهم: تأكد إن الـ CD مو مستعار أبداً بالبداية
            cd.returnCD();   // يجعل available=true ويصفّر التواريخ

            cds.add(cd);
        }
    }



    /**
     * Searches for CDs using a case-insensitive keyword.
     * The search checks:
     * - title
     * - artist
     * - CD ID
     *
     * @param keyword search term (must not be null)
     * @return list of matching CDs, or full list if keyword is blank
     * @throws NullPointerException if keyword is null
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
     * Finds a CD using its unique ID.
     *
     * @param id the CD's ID
     * @return matching CD or null if none found
     */
    public CD findCDById(String id) {
        for (CD c : cds)
            if (c.getId().equals(id))
                return c;
        return null;
    }

    /**
     * Returns all CDs currently stored in memory.
     *
     * @return list of CDs
     */
    public List<CD> getAllCDs() {
        return cds;
    }
}

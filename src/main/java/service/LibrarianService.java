package service;

import domain.CDLoan;
import domain.Librarian;
import domain.Loan;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This service class manages all Librarian-related operations.
 *
 * Responsibilities:
 *  - registering new librarians
 *  - logging librarians in/out
 *  - maintaining a single logged-in librarian at a time
 *  - retrieving overdue book loans (US 2.2 requirement)
 *  - retrieving overdue CD loans
 *  - saving/loading librarian data to/from a file
 *
 * The service depends on LibraryService to obtain overdue items
 * and other circulation data. Librarians themselves are stored
 * in memory and synchronized with a text file.
 *
 * NOTE: Librarians do NOT manage system-wide admin actions.
 */
public class LibrarianService {

    private final List<Librarian> librarians = new ArrayList<>();
    private final LibraryService libraryService;
    private Librarian loggedInLibrarian = null;

    /** Storage file for all librarians. */
    private static final String LIBRARIANS_FILE = "src/main/resources/data/librarians.txt";

    /**
     * Creates a LibrarianService linked to the main LibraryService.
     *
     * @param libraryService central service providing access to loans and books
     */
    public LibrarianService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // -------------------- Registration --------------------

    /**
     * Registers a new librarian.
     * A librarian can only be added if their ID is unique.
     * When a librarian is successfully added, the updated list
     * is saved to the storage file.
     *
     * @param id       unique librarian ID
     * @param name     librarian name
     * @param password login password
     * @return true if the librarian was added, false if ID already exists
     */
    public boolean addLibrarian(int id, String name, String password) {

        for (Librarian l : librarians) {
            if (l.getLibrarianId() == id) {
                return false;  // Duplicate ID
            }
        }

        librarians.add(new Librarian(id, name, password));
        saveLibrariansToFile();
        return true;
    }

    // -------------------- Authentication --------------------

    /**
     * Logs a librarian in.
     * Only one librarian can be logged in at a time.
     *
     * @param name     login name
     * @param password login password
     * @return true if login succeeds, false otherwise
     */
    public boolean login(String name, String password) {

        // Only one active login allowed
        if (loggedInLibrarian != null) {
            return false;
        }

        for (Librarian l : librarians) {
            if (l.login(name, password)) {
                loggedInLibrarian = l;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the currently logged-in librarian.
     * Does nothing if no librarian is logged in.
     */
    public void logout() {
        if (loggedInLibrarian != null) {
            loggedInLibrarian.logout();
            loggedInLibrarian = null;
        }
    }

    /**
     * Checks whether a librarian is currently logged in.
     *
     * @return true if a librarian is logged in, otherwise false
     */
    public boolean isLoggedIn() {
        return loggedInLibrarian != null && loggedInLibrarian.isLoggedIn();
    }

    /**
     * Returns the currently logged-in librarian.
     *
     * @return the active logged-in librarian, or null if none
     */
    public Librarian getLoggedInLibrarian() {
        return loggedInLibrarian;
    }

    /**
     * Returns a list of all registered librarians.
     *
     * @return list containing all librarians
     */
    public List<Librarian> getAllLibrarians() {
        return librarians;
    }

    // -------------------- Overdue Detection (US 2.2) --------------------

    /**
     * Retrieves all overdue book loans.
     * Uses LibraryService logic to determine overdue items.
     *
     * @return a list of overdue book loans
     */
    public List<Loan> getOverdueBooks() {
        return libraryService.getOverdueLoans();
    }

    /**
     * Retrieves all overdue CD loans.
     *
     * @return a list of overdue CD loans
     */
    public List<CDLoan> getOverdueCDs() {
        return libraryService.getOverdueCDLoans();
    }

    /**
     * Returns the total number of overdue items (books + CDs).
     *
     * @return count of overdue items
     */
    public int getTotalOverdueItems() {
        return getOverdueBooks().size() + getOverdueCDs().size();
    }

    // -------------------- File persistence (librarians.txt) --------------------

    /**
     * Saves all registered librarians to the storage file.
     * Each librarian is written as a single line:
     * <pre>
     *     id,name,password
     * </pre>
     */
    private void saveLibrariansToFile() {

        List<String> lines = new ArrayList<>();

        for (Librarian l : librarians) {
            String line = l.getLibrarianId() + "," +
                    l.getName() + "," +
                    l.getPassword();
            lines.add(line);
        }

        FileManager.writeLines(LIBRARIANS_FILE, lines);
    }

    /**
     * Loads librarian data from the storage file and rebuilds the internal list.
     *
     * This method is defensive and will:
     *  - ignore null or blank lines
     *  - ignore malformed lines with missing fields
     *  - ignore lines where the ID is not a valid integer
     *
     * All loaded librarians start in a logged-out state.
     */
    public void loadLibrariansFromFile() {

        librarians.clear();

        List<String> lines = FileManager.readLines(LIBRARIANS_FILE);
        if (lines == null) {
            return;
        }

        for (String line : lines) {

            if (line == null || line.isBlank()) {
                continue;
            }

            String[] p = line.split(",");

            if (p.length < 3) {
                continue;
            }

            String idStr = p[0];
            String name = p[1];
            String password = p[2];

            int id;
            try {
                id = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                // Invalid ID, skip this line
                continue;
            }

            Librarian librarian = new Librarian(id, name, password);
            librarians.add(librarian);
        }
    }
}

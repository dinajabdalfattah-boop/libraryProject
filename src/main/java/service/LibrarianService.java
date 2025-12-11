package service;

import domain.CDLoan;
import domain.Librarian;
import domain.Loan;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides services for managing librarians in the library system.
 * This class handles librarian registration, authentication (login/logout),
 * persistence (load/save), and access to overdue items through LibraryService.
 */
public class LibrarianService {

    private final List<Librarian> librarians = new ArrayList<>();
    private final LibraryService libraryService;
    private Librarian loggedInLibrarian = null;

    private static final String LIBRARIANS_FILE = "src/main/resources/data/librarians.txt";

    /**
     * Constructs a LibrarianService with a dependency on LibraryService.
     *
     * @param libraryService the service used to access overdue books and CDs
     */
    public LibrarianService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * Adds a new librarian if the provided ID is unique.
     * The updated list is saved to the storage file after a successful insert.
     *
     * @param id the unique librarian ID
     * @param name the librarian name
     * @param password the librarian password
     * @return true if the librarian was added, false if the ID already exists
     */
    public boolean addLibrarian(int id, String name, String password) {

        for (Librarian l : librarians) {
            if (l.getLibrarianId() == id) {
                return false;
            }
        }

        librarians.add(new Librarian(id, name, password));
        saveLibrariansToFile();
        return true;
    }

    /**
     * Attempts to log in a librarian using the provided credentials.
     * Login is rejected if another librarian is already logged in.
     *
     * @param name the librarian name entered during login
     * @param password the password entered during login
     * @return true if login succeeds, false otherwise
     */
    public boolean login(String name, String password) {

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
     * Logs out the currently logged-in librarian, if any.
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
     * @return true if a librarian is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loggedInLibrarian != null && loggedInLibrarian.isLoggedIn();
    }

    /**
     * Returns the currently logged-in librarian.
     *
     * @return the logged-in librarian, or null if none is logged in
     */
    public Librarian getLoggedInLibrarian() {
        return loggedInLibrarian;
    }

    /**
     * Returns all librarians currently loaded in memory.
     *
     * @return a list of librarians
     */
    public List<Librarian> getAllLibrarians() {
        return librarians;
    }

    /**
     * Returns all overdue book loans using the underlying LibraryService.
     *
     * @return a list of overdue book loans
     */
    public List<Loan> getOverdueBooks() {
        return libraryService.getOverdueLoans();
    }

    /**
     * Returns all overdue CD loans using the underlying LibraryService.
     *
     * @return a list of overdue CD loans
     */
    public List<CDLoan> getOverdueCDs() {
        return libraryService.getOverdueCDLoans();
    }

    /**
     * Returns the total number of overdue items (books + CDs).
     *
     * @return the total overdue items count
     */
    public int getTotalOverdueItems() {
        return getOverdueBooks().size() + getOverdueCDs().size();
    }

    /**
     * Saves all librarians to the storage file using a comma-separated format:
     * librarianId,name,password
     */
    public void saveLibrariansToFile() {

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
     * Loads librarians from the storage file into memory.
     * Invalid or incomplete lines are ignored.
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
                continue;
            }

            Librarian librarian = new Librarian(id, name, password);
            librarians.add(librarian);
        }
    }
}

package service;

import domain.CDLoan;
import domain.Librarian;
import domain.Loan;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

public class LibrarianService {

    private final List<Librarian> librarians = new ArrayList<>();
    private final LibraryService libraryService;
    private Librarian loggedInLibrarian = null;

    private static final String LIBRARIANS_FILE = "src/main/resources/data/librarians.txt";

    public LibrarianService(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public boolean addLibrarian(int id, String name, String password) {

        for (Librarian l : librarians) {
            if (l.getLibrarianId() == id) {
                return false;
            }
        }

        librarians.add(new Librarian(id, name, password));
        saveLibrariansToFile();   // 🔥 يبقى كما هو
        return true;
    }

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

    public void logout() {
        if (loggedInLibrarian != null) {
            loggedInLibrarian.logout();
            loggedInLibrarian = null;
        }
    }

    public boolean isLoggedIn() {
        return loggedInLibrarian != null && loggedInLibrarian.isLoggedIn();
    }

    public Librarian getLoggedInLibrarian() {
        return loggedInLibrarian;
    }

    public List<Librarian> getAllLibrarians() {
        return librarians;
    }

    public List<Loan> getOverdueBooks() {
        return libraryService.getOverdueLoans();
    }

    public List<CDLoan> getOverdueCDs() {
        return libraryService.getOverdueCDLoans();
    }

    public int getTotalOverdueItems() {
        return getOverdueBooks().size() + getOverdueCDs().size();
    }

    /**
     * Changed from private → public
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

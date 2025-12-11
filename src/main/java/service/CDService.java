package service;

import domain.CD;
import file.FileManager;

import java.util.ArrayList;
import java.util.List;

public class CDService {

    private final List<CD> cds = new ArrayList<>();
    private static final String CD_FILE = "src/main/resources/data/cds.txt";

    public boolean addCD(String title, String artist, String id) {

        if (findCDById(id) != null)
            return false;

        CD cd = new CD(title, artist, id);
        cds.add(cd);

        saveCDsToFile();
        return true;
    }

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

            cd.returnCD();

            cds.add(cd);
        }
    }

    /**
     * search() — EXACT for artist & ID, PARTIAL for title
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

            // title → partial
            if (c.getTitle().toLowerCase().contains(keyLower)) {
                result.add(c);
                continue;
            }

            // artist → exact
            if (c.getArtist().equalsIgnoreCase(keyword)) {
                result.add(c);
                continue;
            }

            // ID → exact
            if (c.getId().equals(keyword)) {
                result.add(c);
            }
        }

        return result;
    }

    public CD findCDById(String id) {
        for (CD c : cds)
            if (c.getId().equals(id))
                return c;
        return null;
    }

    public List<CD> getAllCDs() {
        return cds;
    }
}

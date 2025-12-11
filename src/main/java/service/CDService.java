package service;

import domain.CD;
import file.FileManager;

import java.time.LocalDate;
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

    // ===== helpers =====
    private boolean parseBooleanStrict(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.isEmpty()) return false;
        if (s.equalsIgnoreCase("null")) return false;
        return Boolean.parseBoolean(s);
    }

    private LocalDate parseDateOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("null")) return null;
        return LocalDate.parse(s);
    }
    // ===================

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

            // ✅ availability from file (null => false)
            String availableStr = (p.length > 3) ? p[3] : null;
            boolean available = parseBooleanStrict(availableStr);

            if (available) {
                // available => ignore dates
                cd.returnCD();
            } else {
                // borrowed => read dates
                String borrowStr = (p.length > 4) ? p[4] : null;
                String dueStr = (p.length > 5) ? p[5] : null;

                cd.setBorrowDate(parseDateOrNull(borrowStr));
                cd.setDueDate(parseDateOrNull(dueStr));

                // mark as borrowed
                cd.borrowCD(cd.getBorrowDate());
            }

            cds.add(cd);
        }
    }

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

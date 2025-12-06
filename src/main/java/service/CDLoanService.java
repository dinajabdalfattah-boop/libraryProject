package service;

import domain.*;
import file.FileManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CDLoanService {

    private final List<CDLoan> cdLoans = new ArrayList<>();
    private final BookService bookService;
    private final UserService userService;

    private static final String LOANS_FILE = "src/main/resources/data/cdloans.txt";

    public CDLoanService(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    // ---------------------------------------------------------
    // CREATE CD LOAN
    // ---------------------------------------------------------

    public boolean createCDLoan(User user, CD cd) {

        // NEW FIX — prevent NullPointerException
        if (user == null || cd == null) {
            return false;
        }

        // Borrow restrictions
        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;

        // Prevent borrowing an already borrowed CD
        if (!cd.isAvailable()) return false;

        // Create loan
        CDLoan loan = new CDLoan(user, cd);

        user.addCDLoan(loan);
        cdLoans.add(loan);

        saveLoanToFile(loan);

        return true;
    }

    // ---------------------------------------------------------
    // RETURN CD LOAN
    // ---------------------------------------------------------

    public boolean returnCDLoan(User user, CD cd) {

        for (CDLoan loan : cdLoans) {

            if (loan.getUser().equals(user)
                    && loan.getCD().equals(cd)
                    && loan.isActive()) {

                loan.returnCD();
                user.returnCDLoan(loan);
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------
    // SAVE TO FILE
    // ---------------------------------------------------------

    private void saveLoanToFile(CDLoan loan) {

        String line = String.join(",",
                loan.getUser().getUserName(),
                loan.getCD().getId(),
                loan.getBorrowDate().toString(),
                loan.getDueDate().toString(),
                String.valueOf(loan.isActive())
        );

        FileManager.appendLine(LOANS_FILE, line);
    }

    // ---------------------------------------------------------
    // LOAD FROM FILE
    // ---------------------------------------------------------

    public void loadCDLoansFromFile(List<CD> cds) {

        cdLoans.clear();

        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {
            if (line.isBlank()) continue;

            String[] p = line.split(",");

            String userName = p[0];
            String cdId = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);
            boolean active = Boolean.parseBoolean(p[4]);

            User user = userService.findUserByName(userName);
            CD cd = cds.stream()
                    .filter(c -> c.getId().equals(cdId))
                    .findFirst()
                    .orElse(null);

            if (user == null || cd == null) continue;

            CDLoan loan = new CDLoan(user, cd);
            loan.setBorrowDate(borrowDate);
            loan.setDueDate(dueDate);

            if (!active) loan.returnCD();

            cdLoans.add(loan);

            if (active) user.addCDLoan(loan);
        }
    }

    // ---------------------------------------------------------
    // OVERDUE
    // ---------------------------------------------------------

    public List<CDLoan> getOverdueCDLoans() {
        List<CDLoan> result = new ArrayList<>();

        for (CDLoan loan : cdLoans) {
            if (loan.isOverdue()) {
                result.add(loan);
            }
        }
        return result;
    }

    // ---------------------------------------------------------
    // GETTERS
    // ---------------------------------------------------------

    public List<CDLoan> getAllCDLoans() {
        return cdLoans;
    }
}

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

    public boolean createCDLoan(User user, CD cd) {

        if (user == null || cd == null) {
            return false;
        }

        if (user.getFineBalance() > 0) return false;
        if (user.hasOverdueLoans()) return false;

        if (!cd.isAvailable()) return false;

        CDLoan loan = new CDLoan(user, cd);

        user.addCDLoan(loan);
        cdLoans.add(loan);

        saveAllLoansToFile();
        return true;
    }

    public boolean returnCDLoan(User user, CD cd) {

        if (user == null || cd == null) {
            return false;
        }

        for (CDLoan loan : cdLoans) {

            if (loan == null) continue;

            User loanUser = loan.getUser();
            CD loanCd = loan.getCD();

            if (loanUser == null || loanCd == null) {
                continue;
            }

            if (loanUser.equals(user)
                    && loanCd.equals(cd)
                    && loan.isActive()) {

                loan.returnCD();
                user.returnCDLoan(loan);

                saveAllLoansToFile();
                return true;
            }
        }
        return false;
    }

    public void saveAllLoansToFile() {

        List<String> lines = new ArrayList<>();

        for (CDLoan loan : cdLoans) {

            if (loan == null
                    || loan.getUser() == null
                    || loan.getCD() == null
                    || loan.getBorrowDate() == null
                    || loan.getDueDate() == null) {
                continue;
            }

            String line = String.join(",",
                    loan.getUser().getUserName(),
                    loan.getCD().getId(),
                    loan.getBorrowDate().toString(),
                    loan.getDueDate().toString(),
                    String.valueOf(loan.isActive())
            );

            lines.add(line);
        }

        FileManager.writeLines(LOANS_FILE, lines);
    }

    public void loadCDLoansFromFile(List<CD> cds) {

        cdLoans.clear();

        List<String> lines = FileManager.readLines(LOANS_FILE);
        if (lines == null) return;

        for (String line : lines) {

            if (line == null || line.isBlank()) continue;

            String[] p = line.split(",");
            if (p.length < 5) continue;

            String userName = p[0];
            String cdId = p[1];
            LocalDate borrowDate = LocalDate.parse(p[2]);
            LocalDate dueDate = LocalDate.parse(p[3]);
            boolean active = Boolean.parseBoolean(p[4]);

            User user = userService.findUserByName(userName);
            CD cd = cds.stream()
                    .filter(c -> c != null && c.getId().equals(cdId))
                    .findFirst()
                    .orElse(null);

            if (user == null || cd == null) continue;

            if (active) {
                cd.returnCD();
                cd.borrowCD(borrowDate);
                cd.setDueDate(dueDate);
            } else {
                cd.returnCD();
            }

            CDLoan loan = new CDLoan(user, cd, borrowDate, dueDate, active);
            cdLoans.add(loan);

            if (active) {
                user.getActiveCDLoans().add(loan);
            }
        }
    }


    public List<CDLoan> getOverdueCDLoans() {
        List<CDLoan> result = new ArrayList<>();

        for (CDLoan loan : cdLoans) {
            if (loan != null && loan.isOverdue()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<CDLoan> getAllCDLoans() {
        return cdLoans;
    }
}

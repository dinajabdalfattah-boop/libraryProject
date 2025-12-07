package domain.fine;

/**
 * Fine strategy for overdue book loans.
 * Books have a fixed fine rate: 10 NIS per overdue day.
 * This class implements the FineStrategy interface and provides
 * the calculation logic that the Loan class uses when computing fines.
 */
public class BookFineStrategy implements FineStrategy {

    /**
     * Calculates the fine for a book based on the number of overdue days.
     * Each overdue day costs 10 NIS.
     *
     * @param overdueDays number of days the item is overdue
     * @return the total fine amount in NIS
     */
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 10; // 10 NIS per day
    }
}

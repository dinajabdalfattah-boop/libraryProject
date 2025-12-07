package domain.fine;

/**
 * Fine strategy for overdue CD loans.
 * CDs have a higher daily fine compared to books. The fine rate
 * for CDs is 20 NIS for every overdue day.
 *
 * This class implements the FineStrategy interface and provides
 * the calculation formula used by CDLoan objects.
 */
public class CDFineStrategy implements FineStrategy {

    /**
     * Calculates the fine for a CD based on how many days it is overdue.
     * Each overdue day costs 20 NIS.
     *
     * @param overdueDays number of days the CD is overdue
     * @return the total fine amount in NIS
     */
    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 20; // 20 NIS per day
    }
}

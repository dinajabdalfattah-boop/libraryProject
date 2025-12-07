package domain.fine;

/**
 * Interface representing a strategy for calculating overdue fines.
 * Different media types (such as books or CDs) can implement this interface
 * to provide their own fine calculation rules.
 *
 * The Strategy Pattern allows the system to choose the appropriate
 * fine calculation logic at runtime based on the loan type.
 */
public interface FineStrategy {

    /**
     * Calculates the fine amount based on the number of overdue days.
     *
     * @param overdueDays how many days the item is overdue
     * @return the resulting fine amount
     */
    int calculateFine(int overdueDays);
}

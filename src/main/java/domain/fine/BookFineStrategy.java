package domain.fine;

public class BookFineStrategy implements FineStrategy {

    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 10; // 10 NIS per day
    }
}

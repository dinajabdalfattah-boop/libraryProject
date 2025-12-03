package domain.fine;

public class CDFineStrategy implements FineStrategy {

    @Override
    public int calculateFine(int overdueDays) {
        return overdueDays * 20; // 20 NIS per day
    }
}

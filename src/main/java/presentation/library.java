package presentation;

import domain.Book;
import domain.User;
import notification.EmailNotifier;
import service.LibraryService;
import service.ReminderService;

import java.time.LocalDate;

public class library {
    public static void main(String[] args) {

        ReminderService reminderService = new ReminderService();
        reminderService.addObserver(new EmailNotifier()); // mock email
        LibraryService libraryService = new LibraryService(reminderService);


        User user1 = new User("Dina");
        User user2 = new User("Ali");
        User user3 = new User("Sara");

        libraryService.addUser(user1);
        libraryService.addUser(user2);
        libraryService.addUser(user3);

        Book book1 = new Book("Java 101", "Author A", "ISBN001");
        Book book2 = new Book("Python 101", "Author B", "ISBN002");
        Book book3 = new Book("C++ Basics", "Author C", "ISBN003");
        Book book4 = new Book("Algorithms", "Author D", "ISBN004");

        libraryService.addBook(book1);
        libraryService.addBook(book2);
        libraryService.addBook(book3);
        libraryService.addBook(book4);

        System.out.println("\n--- Scenario 1: User has overdue books ---");
        book1.borrowBook(LocalDate.now().minusDays(30));
        user1.getBorrowedBooks().add(book1); // إضافة يدوياً للقائمة
        libraryService.borrowBook(user1, book2);

        System.out.println("\n--- Scenario 2: User has unpaid fines ---");
        user2.setFineBalance(50); // غرامة غير مدفوعة
        libraryService.borrowBook(user2, book3);

        System.out.println("\n--- Scenario 3: Unregister user with active loans or fines ---");
        libraryService.unregisterUser(user1);
        libraryService.unregisterUser(user2);

        System.out.println("\n--- Scenario 4: Unregister user allowed ---");
        libraryService.unregisterUser(user3); // لا يوجد قروض أو غرامات

        System.out.println("\n--- Sending overdue reminders ---");
        libraryService.sendOverdueReminders();

        System.out.println("\nSprint 4 simulation completed.");
    }
}

package Domain;

public class Book {
    private final String title;
    private final String author;
    private final String isbn;
    private boolean available;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.available = true;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getIsbn() {
        return isbn;
    }
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        String status = available ? "Available " : "Not Available ";
        return " Title : " + title + " ,  Author : " + author + " ,  ISBN : " + isbn + " , " + status;
    }

}


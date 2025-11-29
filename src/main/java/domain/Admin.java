package domain;

public class Admin {
    private String userName;
    private final int adminId;
    private String password;

    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public int getAdminId() {
        return adminId;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

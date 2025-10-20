public class Admin {
    private String userName;
    private int adminId;
    private String password;

    public Admin(String userName, int adminId, String password) {
        this.userName = userName;
        this.adminId = adminId;
        this.password = password;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public int getAdminId() {
        return adminId;
    }

    // ✅ US1.1 Admin Login
    public boolean login(String userName, String password) {
        if (this.userName.equals(userName) && this.password.equals(password)) {
            System.out.println("✅ Login successful. Welcome, " + this.userName + "!");
            return true;
        } else {
            System.out.println("❌ Invalid username or password!");
            return false;
        }
    }

    // ✅ US1.2 Admin Logout
    public void logout() {
        System.out.println("👋 You have been logged out successfully.");
    }
}

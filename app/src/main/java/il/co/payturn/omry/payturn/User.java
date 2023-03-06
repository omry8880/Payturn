package il.co.payturn.omry.payturn;

public class User {
    private String userID; //E-mail
    private String name;
    private String email;
    private String password;

    public User(String userID, String name, String email, String password) {
        String[] parts = userID.split("@");
        parts[0] = parts[0].replace(",", "");
        this.userID = parts[0];
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User() {

    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword() {
        this.password = password;
    }
}

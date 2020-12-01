import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Buyer implements Serializable{
    private String email;
    private int userId;

    public Buyer(String email) {
        this.email = email;
        userId = new Random().nextInt(100000);
        System.out.println("userID generated: " + userId);
    }

    public int getUserId() {
        return userId;
    }

    public String getKeyLocation() {
        return String.valueOf(userId);
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        Buyer buyer = (Buyer) o;
        return userId == buyer.getUserId();
    }
}

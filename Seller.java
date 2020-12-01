import java.io.Serializable;
import java.util.Random;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Seller implements Serializable{
    private String email;
    private int userId;
    private Map<Integer, AuctionItem> sellings; // <auction_id, auction_item>

    public Seller(String email) {
        this.email = email;
        userId = new Random().nextInt(100000);
        System.out.println("userID generated: " + userId);
        sellings = new HashMap<>();
    }

    public String enlistSellings() {
        System.out.println("enlisting auctions..");
        StringBuilder sb = new StringBuilder();
        for (Integer auctionId : sellings.keySet()) {
            if (!sellings.get(auctionId).isClosed()) {
                sb.append(sellings.get(auctionId));
                
            }
        }
        return sb.toString();
    }

    public void enlistSellingsWithId() {
        System.out.println("enlisting auctions..");
        for (Integer auctionId : sellings.keySet()) {
            System.out.println(sellings.get(auctionId));
            System.out.println("AuctionID: " + auctionId);
            System.out.println();
        }
    }

    public String getKeyLocation() {
        return String.valueOf(userId);
    }

    public synchronized void addSelling(int auctionId, AuctionItem ai) {
        sellings.put(auctionId, ai);
    }

    public Map<Integer, AuctionItem> getSellings() {
        return sellings;
    }

    public String getEmail() {
        return email;
    }

    public int getUserId() {
        return userId;
    }

    public void setSellings(Map<Integer, AuctionItem> newList) {
        sellings = newList;
    }

    public synchronized void removeSelling(int auctionId) {
        sellings.get(auctionId).setClosed(true);
        System.out.println("removed:  "+ sellings.remove(auctionId));
        enlistSellings();
    }

    public List<AuctionItem> getAllAuctionItems() {
        List<AuctionItem> result = new ArrayList<>();
        for (int auctionId : sellings.keySet()) {
            result.add(sellings.get(auctionId));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        Seller s = (Seller) o;
        return userId == s.getUserId();
    }
}

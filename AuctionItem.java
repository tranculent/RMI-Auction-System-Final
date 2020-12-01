import java.io.Serializable;

public class AuctionItem implements Serializable {
    private int itemId;
    private int startingPrice;
    private String itemTitle;
    private String itemDescription;
    private int highestBid;
    private Buyer currentBuyer;
    private int minPrice;
    private Seller seller;
    private boolean closed;
    
    public AuctionItem(Seller seller, int itemId, int startingPrice, int minPrice, String itemTitle, String itemDescription) {
        this.itemId = itemId;
        this.startingPrice = startingPrice;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        highestBid = startingPrice;
        currentBuyer = null;
        this.minPrice = minPrice;
        this.seller = seller;
        closed = false;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("Information about item with ID " + itemId + ": ");
        sb.append("\n");
        sb.append("Title: " + itemTitle);
        sb.append("\n");
        sb.append("Description: " + itemDescription);
        sb.append("\n");
        sb.append("Starting price: " + startingPrice);
        sb.append("\n");
        sb.append("Seller: " + seller.getEmail());
        sb.append("\n");
        
        if (currentBuyer != null) {
            sb.append("Highest bid: " + highestBid);
            sb.append("\n");
            sb.append("Current buyer: " + currentBuyer.getEmail());
            sb.append("\n");
        }
        else {
            sb.append("Status: No buyer have bidded or beaten the minimum price for this item.");
            sb.append("\n");
        }
        return sb.toString();
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setStartingPrice(int startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setHighestBid(int highestBid) {
        this.highestBid = highestBid;
    }

    public void setNewBuyer(Buyer newBuyer) {
        currentBuyer = newBuyer;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getItemId() {
        return itemId;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public int getHighestBid() {
        return highestBid;
    }

    public Buyer getCurrentBuyer() {
        return currentBuyer;
    }

    public int getMinPrice() {
        return minPrice;
    }

    @Override
    public boolean equals(Object o) {
        AuctionItem ai = (AuctionItem) o;
        return itemId == ai.getItemId();
    }
}

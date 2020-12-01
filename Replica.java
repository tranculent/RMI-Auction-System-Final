import java.util.*;

import javax.crypto.SecretKey;
import javax.management.RuntimeErrorException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import java.security.SecureRandom;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.io.File;  // Import the File class
import java.io.FileInputStream;

import javax.crypto.BadPaddingException;
import java.io.*;

import org.jgroups.*;
import org.jgroups.blocks.*;
import org.jgroups.Message.TransientFlag;
import org.jgroups.util.*;

public class Replica {
    private Key key;
    private List<Seller> sellers = new ArrayList<>();
    private List<Buyer> buyers = new ArrayList<>();
    private int lastID = 0;
    private int confirmSellerIdentity = -1;
    private int confirmBuyerIdentity = -1;

    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RequestOptions opts;

    private RspList rspList;

    public static void main(String[] args) throws java.rmi.RemoteException, Exception, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        new Replica();
    }

    public Replica() throws java.rmi.RemoteException, Exception, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        channel = new JChannel();
        opts = new RequestOptions(ResponseMode.GET_ALL, 1000L).setTransientFlags(TransientFlag.DONT_LOOPBACK);
        dispatcher = new RpcDispatcher(channel,this);
        channel.connect("MyCluster");

        rspList = dispatcher.callRemoteMethods(null, "updateSellers", new Object[] {}, new Class[] {}, opts);
        sellers = majority() == null ? new ArrayList<>() : (List) majority();

        rspList = dispatcher.callRemoteMethods(null, "updateBuyers", new Object[] {}, new Class[] {}, opts);
        buyers = majority() == null ? new ArrayList<>() : (List) majority();

        rspList = dispatcher.callRemoteMethods(null, "updateLastId", new Object[] {}, new Class[] {}, opts);
        lastID = majority() == null ? 0 : (int) majority();
    }

    public List<Seller> updateSellers() {
        List<Seller> res = new ArrayList<>();
        for (Seller s : sellers) res.add(s);
        return res;
    }
    
    public List<Buyer> updateBuyers() {
        List<Buyer> res = new ArrayList<>();
        for (Buyer b : buyers) res.add(b);
        return res;
    }

    public int updateLastId() {
        return lastID;
    }

    /**
     * Returns the most common response list.
     * @param responseList the response list
     * @return the most common response list
     */
    private Object majority() throws Exception {
        Map<Object, Integer> result = new HashMap<>();
        
        if (rspList.getResults().isEmpty()) return null;

        System.out.println("Responses:" + rspList);
        for (Object response : rspList.getResults()) {
            // System.out.println("Response: " + response);
            if (!result.containsKey(response))
                result.put(response, 1);
            else 
                result.put(response, result.get(response)+1);
        }

        return Collections.max(result.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }

    /**
     * Returns the auction item based on the provided item id. The seller is also required to be able to extract the userID and find the 
     * associated key.
     * @param s the seller to extract the sealed object
     * @param itemId the item id of the auction item
     * @return sealed object containing the auction item
     */
    public AuctionItem getAuctionItemById(int itemId) throws java.rmi.RemoteException, NoSuchAlgorithmException,
            InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        for (Seller seller : sellers) {
            for (int i = 0; i < seller.getAllAuctionItems().size(); i++) {
                if (seller.getAllAuctionItems().get(i).getItemId() == itemId) {
                    return seller.getAllAuctionItems().get(i);
                }
            }
        }

        return null;
    }

    /**
     * Retrieves the seller by item id.
     * @param itemId
     * @return
     * @throws RemoteException
     */
    public Seller getSellerByItemId(int itemId) throws RemoteException {
        for (Seller seller : sellers) {
            for (Integer auctionId : seller.getSellings().keySet()) {
                if (seller.getSellings().get(auctionId).getItemId() == itemId) {
                    return seller;
                }
            }
        }
        return null;
    }

    /**
     * Displays all the available actions.
     * @return string containing all the auctions
     * @throws RemoteException
     */
    public String enlistAvailableAuctions() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (Seller seller : sellers){
            System.out.println(seller.enlistSellings());
            sb.append(seller.enlistSellings());
        }
        return sb.toString();
    }

    /**
     * Adds an auction item onto the seller's list of items.
     * @param ai the auction item to be added
     * @param seller the seller who will be the owner of the item
     * @param auctionId the key for the map of the seller
     * @throws RemoteException
     */
    public void addAuctionItem(AuctionItem ai, Seller seller, int auctionId) throws RemoteException {
        for (Seller s: sellers) {
            if (s.getEmail().equals(seller.getEmail())) {
                s.addSelling(auctionId, ai);
            }
        }
        lastID++;
    }

    /**
     * Removes an auction item based on the auction id.
     * @param ai the uaction item to be removed
     * @param auctionId the auction id of the item
     * @throws RemoteException
     */
    public void removeAuctionItem(AuctionItem ai, int auctionId) throws RemoteException {
        Seller s = null;
        for (Seller seller : sellers) {
            for (int aid : seller.getSellings().keySet()) {
                if (seller.getSellings().get(aid).getItemId() == ai.getItemId()) {
                    s = seller;
                }
            }
        }
        s.removeSelling(auctionId);
    }        

    /**
     * Gets the last id.
     * @return the last id
     * @throws RemoteException
     */
    public int getLastID() throws RemoteException {
        return lastID;
    }

    /**
     * Overwrites the auction item's information based on the buyer's bid.
     * @param itemId the id of the item
     * @param newBid the new bid value
     * @param newBuyer the new buyer who made the bid
     * @throws RemoteException
     */
    public synchronized void overwriteBid(int itemId, int newBid, Buyer newBuyer) throws RemoteException {
        // search for the seller
        for (Seller seller : sellers) {
            // get the sellings for the seller
            for (int auctionId : seller.getSellings().keySet()) {
                // overwrite that selling with the new buyer
                if (seller.getSellings().get(auctionId).getItemId() == itemId) {
                    seller.getSellings().get(auctionId).setNewBuyer(newBuyer);
                    seller.getSellings().get(auctionId).setHighestBid(newBid);
                }
            }
        }
    }

    /**
     * Returns the size of the auction items.
     * @return the size of the auction items
     * @throws RemoteException
     */
    public int size() throws RemoteException {
        int size = 0;

        for (Seller seller : sellers) {
            size += seller.getAllAuctionItems().size();
        }

        return size;
    }

    /**
     * Adds seller to the server.
     * @param seller the seller to be added
     * @throws RemoteException
     */
    public void addSeller(Seller seller) throws RemoteException {
        sellers.add(seller);
    }

    /**
     * Checks if seller exists in the server.
     * @param email the email of the seller
     * @return true or false based on the existence of the seller
     * @throws RemoteException
     */
    public boolean sellerExists(String email) throws RemoteException {
        for (Seller seller : sellers) {
            if (seller.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the seller based on the email.
     * @param email the email of the seller
     * @return the seller
     * @throws RemoteException
     */
    public Seller getSeller(String email) throws RemoteException {
        for (Seller seller : sellers) {
            if (seller.getEmail().equals(email)) {
                return seller;
            }
        }
        return null;
    }

    public Seller getSeller(int index) throws RemoteException {
        return sellers.get(index);
    }

    /**
     * Adds a bueyr to the server.
     * @param buyer the buyer to be added
     * @throws RemoteException
     */
    public void addBuyer(Buyer buyer) throws RemoteException {
        buyers.add(buyer);
    }

    /**
     * Checks if buyer exists or not.
     * @param email the email of the buyer
     * @return true or false based on the existence of the bueyr
     * @throws RemoteException
     */
    public boolean buyerExists(String email) throws RemoteException {
        for (Buyer buyer : buyers) {
            if (buyer.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the buyer.
     * @param email the email of the buyer
     * @return the buyer if he exists
     * @throws RemoteException
     */
    public Buyer getBuyer(String email) throws RemoteException {
        for (Buyer buyer : buyers) {
            if (buyer.getEmail().equals(email)) {
                return buyer;
            }
        }

        return null;
    }

    /**
     * Returns a list of all the sellers that are present in the server.
     * @return the list of sellers
     * @throws RemoteException
     */
    public List<Seller> getSellers() throws RemoteException {
        return sellers;
    }

    public String showAuctionsForSeller(String email) throws RemoteException { 
        StringBuilder builder = new StringBuilder();
        for (AuctionItem ai : getSeller(email).getAllAuctionItems()) {
            builder.append(ai);
        }

        return builder.toString();
    }
}

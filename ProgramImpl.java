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
import org.jgroups.Message.*;
import org.jgroups.util.*;

public class ProgramImpl extends java.rmi.server.UnicastRemoteObject implements Program {
    private Key key;
    private List<Seller> sellers = new ArrayList<>();
    private List<Buyer> buyers = new ArrayList<>();
    private int lastID = 0;
    private int confirmSellerIdentity = -1;
    private int confirmBuyerIdentity = -1;
    private JChannel channel;
    private RpcDispatcher dispatcher;
    private RspList rspList;
    private RequestOptions opts;

    public ProgramImpl() throws java.rmi.RemoteException, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException, Exception {
        super();
        opts = new RequestOptions(ResponseMode.GET_ALL, 1000L).setTransientFlags(TransientFlag.DONT_LOOPBACK);
        channel = new JChannel();
        dispatcher = new RpcDispatcher(channel,this);
        channel.connect("MyCluster");
    }

    /**
     * Sends a challenge to the client to verify its identity.
     * @return a random number
     * @throws RemoteException
     */
    public int challenge() throws RemoteException {
        return new Random().nextInt(100000);
    }

    /**
     * Authenticates the seller by sending a sealed object with the original key of the seller.
     * @param seller the seller to be authenticated
     * @return sealed object containing an encrypted number
     */
    public SealedObject auth(Seller seller) 
    throws RemoteException, Exception, ClassNotFoundException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException {
        Key tempKey;
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(seller.getKeyLocation()+".txt"));
        try {
            tempKey = (Key) oin.readObject();
        }
        finally {
            oin.close();
        }
        key = tempKey;

        Cipher encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, tempKey);

        confirmSellerIdentity = new Random().nextInt(100000);
        SealedObject res = new SealedObject(confirmSellerIdentity, encryptCipher);
        
        return res;
    }

    /**
     * Authenticates the buyer by sending a sealed object with the original key of the buyer.
     * @param buyer the buyer to be authenticated
     * @return sealed object containing an encrypted number
     */
    public SealedObject auth(Buyer buyer) 
    throws RemoteException, Exception, ClassNotFoundException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException {
        Key tempKey;
        ObjectInputStream oin = new ObjectInputStream(new FileInputStream(buyer.getKeyLocation()+".txt"));
        try {
            tempKey = (Key) oin.readObject();
        }
        finally {
            oin.close();
        }
        key = tempKey;

        Cipher encryptCipher = Cipher.getInstance("AES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, tempKey);

        confirmBuyerIdentity = new Random().nextInt(100000);
        SealedObject res = new SealedObject(confirmBuyerIdentity, encryptCipher);
        
        return res;
    }

    /**
     * Confirms the seller identity by carrying out a check for the received and returned values from the encryption/decryption process.
     * @param decryptVal the value provided by the client
     * @return true/false if the client provided the right decryption value or not
     * @throws RemoteException
     */
    public boolean confirmSellerIdentity(int decryptVal) throws RemoteException, Exception {
        return decryptVal == confirmSellerIdentity;
    }

    /**
     * Confirms the buyer identity by carrying out a check for the received and returned values from the encryption/decryption process.
     * @param decryptVal the value provided by the client
     * @return true/false if the client provided the right decryption value or not
     * @throws RemoteException
     */
    public boolean confirmBuyerIdentity(int decryptVal) throws RemoteException, Exception {
        return decryptVal == confirmBuyerIdentity;
    }

    /**
     * Returns the auction item based on the provided item id. The seller is also required to be able to extract the userID and find the 
     * associated key.
     * @param s the seller to extract the sealed object
     * @param itemId the item id of the auction item
     * @return sealed object cotnaining the auction item
     */
    public AuctionItem getAuctionItemById(int itemId) throws java.rmi.RemoteException, Exception, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException {
        rspList = dispatcher.callRemoteMethods(null, "getAuctionItemById", new Object[]{itemId}, new Class[] {int.class}, opts);
        return (AuctionItem) majority();
    }

    /**
     * Retrieves the seller by item id.
     * @param itemId
     * @return
     * @throws RemoteException
     */
    public Seller getSellerByItemId(int itemId) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getSellerByItemId", new Object[]{itemId}, new Class[] {int.class}, opts);
        return (Seller) majority();
    }

    /**
     * Displays all the available actions.
     * @return string containing all the auctions
     * @throws RemoteException
     */
    public String enlistAvailableAuctions() throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "enlistAvailableAuctions", new Object[]{}, new Class[] {}, opts);
        return (String) majority();
    }

    /**
     * Adds an auction item onto the seller's list of items.
     * @param ai the auction item to be added
     * @param seller the seller who will be the owner of the item
     * @param auctionId the key for the map of the seller
     * @throws RemoteException
     */
    public void addAuctionItem(AuctionItem ai, Seller seller, int auctionId) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "addAuctionItem", new Object[]{ai, seller, auctionId}, new Class[] {AuctionItem.class, Seller.class, int.class}, opts);
    }

    /**
     * Removes an auction item based on the auction id.
     * @param ai the uaction item to be removed
     * @param auctionId the auction id of the item
     * @throws RemoteException
     */
    public void removeAuctionItem(AuctionItem ai, int auctionId) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "removeAuctionItem", new Object[]{ai, auctionId}, new Class[] {AuctionItem.class, int.class}, opts);
    }        

    /**
     * Gets the last id.
     * @return the last id
     * @throws RemoteException
     */
    public int getLastID() throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getLastID", new Object[]{}, new Class[] {}, opts);
        System.out.println("LAST ID : " + lastID);
        return (int) majority();
    }

    /**
     * Overwrites the auction item's information based on the buyer's bid.
     * @param itemId the id of the item
     * @param newBid the new bid value
     * @param newBuyer the new buyer who made the bid
     * @throws RemoteException
     */
    public synchronized void overwriteBid(int itemId, int newBid, Buyer newBuyer) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "overwriteBid", new Object[]{itemId, newBid, newBuyer}, new Class[] {int.class, int.class, Buyer.class}, opts);
    }

    /**
     * Returns the size of the auction items.
     * @return the size of the auction items
     * @throws RemoteException
     */
    public int size() throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "size", new Object[]{}, new Class[] {}, opts);
        return (int) majority();
    }

    /**
     * Adds seller to the server.
     * @param seller the seller to be added
     * @throws RemoteException
     */
    public void addSeller(Seller seller) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "addSeller", new Object[]{seller}, new Class[] {Seller.class}, opts);
    }

    /**
     * Checks if seller exists in the server.
     * @param email the email of the seller
     * @return true or false based on the existence of the seller
     * @throws RemoteException
     */
    public boolean sellerExists(String email) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "sellerExists", new Object[]{email}, new Class[] {String.class}, opts);
        return (boolean) majority();
    }

    /**
     * Returns the seller based on the email.
     * @param email the email of the seller
     * @return the seller
     * @throws RemoteException
     */
    public Seller getSeller(String email) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getSeller", new Object[]{email}, new Class[] {String.class}, opts);
        return (Seller) majority();
    }

    public Seller getSeller(int index) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getSeller", new Object[]{index}, new Class[] {int.class}, opts);
        return (Seller) majority();
    }

    /**
     * Adds a bueyr to the server.
     * @param buyer the buyer to be added
     * @throws RemoteException
     */
    public void addBuyer(Buyer buyer) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "addBuyer", new Object[]{buyer}, new Class[] {Buyer.class}, opts);
    }

    /**
     * Checks if buyer exists or not.
     * @param email the email of the buyer
     * @return true or false based on the existence of the bueyr
     * @throws RemoteException
     */
    public boolean buyerExists(String email) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "buyerExists", new Object[]{email}, new Class[] {String.class}, opts);
        return (boolean) majority();
    }

    /**
     * Returns the buyer.
     * @param email the email of the buyer
     * @return the buyer if he exists
     * @throws RemoteException
     */
    public Buyer getBuyer(String email) throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getBuyer", new Object[]{email}, new Class[] {String.class}, opts);
        return (Buyer) majority();
    }

    /**
     * Returns a list of all the sellers that are present in the server.
     * @return the list of sellers
     * @throws RemoteException
     */
    public List<Seller> getSellers() throws RemoteException, Exception {
        rspList = dispatcher.callRemoteMethods(null, "getSellers", new Object[]{}, new Class[] {}, opts);
        return (List<Seller>) majority();
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
}

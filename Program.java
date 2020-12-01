import javax.crypto.SealedObject;
import java.util.*;
// import jdk.jshell.execution.RemoteExecutionControl;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.IllegalBlockSizeException;

import java.util.List;
import java.util.Map;


public interface Program extends java.rmi.Remote {
    public AuctionItem getAuctionItemById(int itemId) throws java.rmi.RemoteException, Exception, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException, ClassNotFoundException;
    public void addAuctionItem(AuctionItem ai, Seller seller, int auctionId) throws RemoteException, Exception;
    public void removeAuctionItem(AuctionItem ai, int auctionId) throws RemoteException, Exception;
    public int getLastID() throws RemoteException, Exception;
    public String enlistAvailableAuctions() throws RemoteException, Exception;
    public void overwriteBid(int itemId, int newBid, Buyer newBuyer) throws RemoteException, Exception;
    public int size() throws RemoteException, Exception;
    public void addSeller(Seller seller) throws RemoteException, Exception;
    public boolean sellerExists(String email) throws RemoteException, Exception;
    public Seller getSeller(String email) throws RemoteException, Exception;
    public void addBuyer(Buyer buyer) throws RemoteException, Exception;
    public boolean buyerExists(String email) throws RemoteException, Exception;
    public Buyer getBuyer(String email) throws RemoteException, Exception;
    public List<Seller> getSellers() throws RemoteException, Exception;
    public int challenge() throws RemoteException, Exception;
    public SealedObject auth(Seller seller) throws RemoteException, Exception, ClassNotFoundException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException;
    public SealedObject auth(Buyer buyer) throws RemoteException, Exception, ClassNotFoundException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException;
    public boolean confirmSellerIdentity(int decryption) throws RemoteException, Exception;
    public boolean confirmBuyerIdentity(int decryption) throws RemoteException, Exception;
    public Seller getSellerByItemId(int itemId) throws RemoteException, Exception;
}

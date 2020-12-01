import java.rmi.Naming;			//Import the rmi naming - so you can lookup remote object
import java.rmi.RemoteException;	//Import the RemoteException class so you can catch it
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;	//Import the MalformedURLException class so you can catch it
import java.rmi.NotBoundException;	//Import the NotBoundException class so you can catch it

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import java.io.*;

public class BuyerClient {
    private static Key key;
    public static void main(String[] args) throws RemoteException, Exception {
        try {
            Program server = (Program) Naming.lookup("rmi://localhost/ProgramService");
            Scanner sc = new Scanner(System.in);

            System.out.println("You can bid on items by typing the item id. After that you can select a bid price. Please note it has to be greater than the highest bid...");
            String email;

            System.out.println("What's your email: ");
            email = sc.nextLine();

            Cipher cipher = Cipher.getInstance("AES");

            Buyer buyer = server.getBuyer(email);
            if (buyer == null) { 
                System.out.println("Hello! Thanks for using my program. You can now bid on the available auctions.");
                buyer = new Buyer(email);
                server.addBuyer(buyer);
                writeKey(buyer.getUserId());

                cipher.init(Cipher.DECRYPT_MODE, key);

                if (server.size() > 0) { 
                    server.enlistAvailableAuctions();
                }
                else {
                    System.out.println("There are no available auctions at the moment. Please come back later!");
                }
            }
            else {
                // authenticate using key
                SealedObject confirmObject = server.auth(buyer);

                Key tempKey;
                System.out.println("Confirming identity..");

                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(buyer.getKeyLocation()+".txt"));
                try {
                    tempKey = (Key) oin.readObject();
                }
                finally {
                    oin.close();
                }
                cipher.init(Cipher.DECRYPT_MODE, tempKey);

                int decryptedItemId = (int) confirmObject.getObject(cipher);
                key = tempKey;

                if (server.confirmBuyerIdentity(decryptedItemId)) {
                    System.out.println("Congratulations! You successfully passed the authentication process.");
                }
            }

            int lastSize = server.size();
            while(server.size() > 0) {
                System.out.println(server.enlistAvailableAuctions());

                if (lastSize < server.size()) {
                    System.out.println("An auction seems to have closed. Here's the updated browsing:");
                    server.enlistAvailableAuctions();
                    lastSize = server.size();
                } else if (lastSize > server.size()) {
                    System.out.println("A new item seems to have been added. Here's the updated browsing:");
                    server.enlistAvailableAuctions();
                }

                System.out.println("Bidding..");
                bid(buyer, email, cipher, sc, server);
            }

            System.out.println("No auctions are found. Plase come back later.");
        }
        catch (NoSuchAlgorithmException nsae) {
            System.out.println(nsae);
        }
        catch (IOException ioexc) {
            System.out.println(ioexc);
        } 
        catch (NoSuchPaddingException nspe) {
            System.out.println(nspe);
        }
        catch (InvalidKeyException ike) {
            System.out.println(ike);
        }
        catch (IllegalBlockSizeException ibse) {
            System.out.println(ibse);
        }
        catch (ClassNotFoundException cnfe) {
            System.out.println(cnfe);
        }
        catch (BadPaddingException bpe) {
            System.out.println(bpe);
        }
        catch (NotBoundException nbe) {
            System.out.println(nbe);
        }
        catch (ArithmeticException ae) {
            System.out.println(ae);
        }
    }

    private static void bid(Buyer buyer, String email, Cipher cipher, Scanner sc, Program server) throws RemoteException, Exception, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, ClassNotFoundException, BadPaddingException, NotBoundException, ArithmeticException {
        System.out.println("Item ID: ");
        int i_id = sc.nextInt();
        System.out.println("Bid: ");
        int b = sc.nextInt();
        
        AuctionItem biddedItem = server.getAuctionItemById(i_id);
        if (b > biddedItem.getHighestBid()) {
            System.out.println("Congratulations, you now hold the highest bid for this item.");
            server.overwriteBid(i_id, b, buyer);
        }
        else {
            System.out.println("Sorry, you bid is not higher than the current highest bid.");
        }
    }

    private static void writeKey(int buyerId) throws FileNotFoundException, IOException, Exception, NoSuchAlgorithmException {
        FileOutputStream out = new FileOutputStream(String.valueOf(buyerId)+".txt");
        ObjectOutputStream oout = new ObjectOutputStream(out);
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        SecureRandom sr = new SecureRandom();
        kg.init(256, sr);
        Key k = kg.generateKey();

        try {
            oout.writeObject(k);
        } finally {
            oout.close();
        }
        System.out.println("Writing key to file...");
        key = k;
    }
}

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
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import java.util.HashMap;

import java.io.*;

public class SellerClient {
    private static Key key;
    public static void main(String[] args) throws RemoteException, Exception, ClassNotFoundException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException, NoSuchPaddingException, IllegalBlockSizeException {

        try {
            Program server = (Program) Naming.lookup("rmi://localhost/ProgramService");
            Scanner sc = new Scanner(System.in);

            System.out.println("What's your email: ");
            String email = sc.nextLine();

            Cipher cipher = Cipher.getInstance("AES");

            Seller seller = server.getSeller(email);
            if (seller == null) {
                System.out.println("Welcome! Since you are a new user, a random user_id and key will be gerenated for you. Please use it login the next time you use the program.");
                seller = new Seller(email);
                writeKey(seller.getUserId());
                server.addSeller(seller);

                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            else {
                // authenticate using key
                SealedObject confirmObject = server.auth(seller);

                Key tempKey;
                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(seller.getKeyLocation()+".txt"));
                try {
                    tempKey = (Key) oin.readObject();
                }
                finally {
                    oin.close();
                }

                cipher.init(Cipher.DECRYPT_MODE, tempKey);

                int decryptedItemId = (int) confirmObject.getObject(cipher);
                key = tempKey;

                if (server.confirmSellerIdentity(decryptedItemId)) {
                    System.out.println("Congratulations! You successfully passed the authentication process.");
                }
                else {
                    System.out.println("You failed the authentication process");
                }
            }
            
            createSelling(seller, server, sc);
            
            while (true) {
                if (server.getSeller(email).getSellings().size() > 0) {
                    sc.nextLine();
                    System.out.println("Please type view/create/delete: ");
                    
                    boolean successfulRemoval = false;
                    String input = sc.nextLine();
                    if (input.equals("delete")) {
                        System.out.println("Confirm auction ID: (If you wish to go back, type 'back')");
                            if (sc.hasNextInt()) {
                                int closeSellingId = sc.nextInt();
                                // System.out.print("\033\143");
                                for (int auctionId : server.getSeller(email).getSellings().keySet()) {
                                    if (auctionId == closeSellingId) {
                                        AuctionItem biddedItem = server.getAuctionItemById(server.getSeller(email).getSellings().get(auctionId).getItemId());
                                        
                                        if (biddedItem.getHighestBid() > biddedItem.getMinPrice()) {
                                            System.out.println("The winner for item " + biddedItem.getItemTitle() + " is " + biddedItem.getCurrentBuyer().getEmail() + " with the highest bid of $" + biddedItem.getHighestBid());
                                            System.out.println("Congratulations!");
                                        }
                                        else {
                                            System.out.println("Unfortunately, no bidder managed to top the reserve/minimum price for " + biddedItem.getItemTitle() + ". Therefore, no winner has been selected.");
                                            System.out.println();
                                        }
                                        System.out.println("Auction" + auctionId + " closed!");
                                        server.removeAuctionItem(biddedItem, auctionId);
                                        
                                        successfulRemoval = true;
                                    }
                                }
                                if (!successfulRemoval) {
                                    System.out.println("No existing item exists with this auction ID. Please try again!");
                                    System.out.println();
                                }


                                System.out.println("Here are your remaining sellings..");
                                System.out.println(server.enlistAvailableAuctions());
                            }
                            else {
                                System.out.println("Please enter a number.");
                            }
                        }
                    else if (input.equals("create")) {
                        createSelling(seller, server, sc);
                    }
                    else if (input.equals("view")) {
                        System.out.println(server.enlistAvailableAuctions());
                    }
                }
                else {
                    System.out.println("You have no sellings. Do you wish to create one? (Y/N)");
                    sc.nextLine();
                    if (sc.nextLine().equals("Y")) {
                        createSelling(seller, server, sc);
                    }
                    else if (sc.nextLine().equals("N")) {
                        return;
                    }
                }
            }
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

    private static void createSelling(Seller seller, Program server, Scanner sc) throws RemoteException, Exception {
        System.out.println();
        System.out.println("Creating a selling....");

        System.out.println("Title: ");
        String itemTitle = sc.nextLine();

        System.out.println("Description: ");
        String description = sc.nextLine();

        System.out.println("Starting price: ");
        int startingPrice = sc.nextInt();
        
        System.out.println("Minimum price: ");
        int minPrice = sc.nextInt();

        int auctionId = new Random().nextInt(100000);
        System.out.println("Auction ID: " + auctionId);

        int itemId = server.getLastID() + 1;
        AuctionItem ai = new AuctionItem(seller, itemId, startingPrice, minPrice, itemTitle, description);
        System.out.println();
        System.out.println(seller.getEmail());
        
        server.addAuctionItem(ai, seller, auctionId);
    }

    private static void writeKey(int sellerId) throws FileNotFoundException, IOException, Exception, NoSuchAlgorithmException {
        FileOutputStream out = new FileOutputStream(String.valueOf(sellerId)+".txt");
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
        System.out.println();
        key = k;
    }
}

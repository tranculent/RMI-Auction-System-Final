import java.rmi.Naming;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.SealedObject;

public class Server {
    public Server() {
        try {
            Program server = new ProgramImpl();
            Naming.rebind("rmi://localhost/ProgramService", server);
        }catch(Exception e) {
            System.out.println(e);
        }
    }

    public static void main (String args[])  {
        new Server();
    }
}
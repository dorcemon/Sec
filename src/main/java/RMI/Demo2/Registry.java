package RMI.Demo2;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Registry {

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            UserService stub = new UserServiceImpl();
            Naming.rebind("RemoteUserService", stub);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

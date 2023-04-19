package RMI.Demo2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserService extends Remote {

    String getUsername(int userId) throws RemoteException;

    User getById(int userId) throws RemoteException;
}

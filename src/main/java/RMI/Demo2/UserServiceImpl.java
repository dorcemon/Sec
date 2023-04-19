package RMI.Demo2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UserServiceImpl extends UnicastRemoteObject implements UserService{
    //因为父类UnicastRemoteObject的构造器均抛出了RemoteException异常
    //所以，UserServiceImpl必须显示声明一个抛出该异常的构造器
    public UserServiceImpl() throws RemoteException {}

    @Override
    public String getUsername(int userId) throws RemoteException {
        return "username:::" + userId;
    }

    @Override
    public User getById(int userId) throws RemoteException {
        User user = new User();
        user.setUserId(userId);
        user.setUsername("username:::" + userId);
        user.setAge(userId*10);
        return user;
    }
}

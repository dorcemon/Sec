package RMI.Demo2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 注意看这里继承了UnicastRemoteObject，关于UnicastRemoteObject，如果想要将对象能够通过RMI服务来进行远程调用的话，
 * 那么该对象就需要继承UnicastRemoteObject，或者是通过UnicastRemoteObject.exportObject来进行导出，返回的对象就是一个Remote对象
 */

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

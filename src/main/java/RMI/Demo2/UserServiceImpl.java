package RMI.Demo2;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class UserServiceImpl extends UnicastRemoteObject implements UserService{
    //��Ϊ����UnicastRemoteObject�Ĺ��������׳���RemoteException�쳣
    //���ԣ�UserServiceImpl������ʾ����һ���׳����쳣�Ĺ�����
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

package RMI.Demo2;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Registry {

    public static void main(String[] args) {
        try {
            //创建Registry实例，并在端口1099监听远程请求
            LocateRegistry.createRegistry(1099);
            //把远程对象（service）注册到Registry
            UserService stub = new UserServiceImpl();
            //将存根（stub）与一个名字（RemoteUserService）绑定，以便调用
            Naming.rebind("RemoteUserService", stub);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

package RMI.Demo2;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Registry {

    public static void main(String[] args) {
        try {
            //����Registryʵ�������ڶ˿�1099����Զ������
            LocateRegistry.createRegistry(1099);
            //��Զ�̶���service��ע�ᵽRegistry
            UserService stub = new UserServiceImpl();
            //�������stub����һ�����֣�RemoteUserService���󶨣��Ա����
            Naming.rebind("RemoteUserService", stub);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}

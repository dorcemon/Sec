package RMI.Demo2;

import java.rmi.Naming;

public class Client {

    public static void main(String[] args) {
        try{
            //�ͻ��˵�RMI registry��Ѱ��
            UserService service = (UserService) Naming.lookup("rmi://127.0.0.1:/RemoteUserService");
            int userId = 10;
            String username = service.getUsername(userId);
            User user = service.getById(userId);

            System.out.println(username);
            System.out.println(user.toString());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
//�������username:::10  User{userId=10, username='username:::10', age=100}

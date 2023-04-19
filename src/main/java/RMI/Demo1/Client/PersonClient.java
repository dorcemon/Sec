package RMI.Demo1.Client;

import RMI.Demo1.Person;

public class PersonClient {
    public static void main(String [] args) throws Throwable {

        Person stub = new PersonStub("127.0.0.1", 1234);
        System.out.println(stub.getName()); // ��� "John"
        //�������ֻ�ܶ�һ��
        System.out.println(stub.getAge()); // ��� "30"
    }
}

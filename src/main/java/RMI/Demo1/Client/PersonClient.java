package RMI.Demo1.Client;

import RMI.Demo1.Person;

public class PersonClient {
    public static void main(String [] args) throws Throwable {

        Person stub = new PersonStub("127.0.0.1", 1234);
        System.out.println(stub.getName()); // 输出 "John"
        //两个输出只能读一个
        System.out.println(stub.getAge()); // 输出 "30"
    }
}

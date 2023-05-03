package RMI.Demo1.Server;

import RMI.Demo1.Person;
import RMI.Demo1.PersonImpl;

import java.io.IOException;

//04/30：这里有些不清楚为什么需要实现person接口。也没地方使用到
public class PersonServer{

    public static void main(String[] args) throws IOException {
        //这里调用了person的实现类
        Person person = new PersonImpl("John", 30);
        PersonSkeleton skeleton = new PersonSkeleton(person, 1234);
        //启动PersonSkeleton中实现的run方法
        skeleton.start();
    }

}

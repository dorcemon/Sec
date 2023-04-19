package RMI.Demo1.Server;

import RMI.Demo1.Person;
import RMI.Demo1.PersonImpl;

import java.io.IOException;

public class PersonServer implements Person {
    private int age;
    private String name;
    public PersonServer(String name, int age) {
        this.age = age;
        this.name = name;
    }
    @Override
    public int getAge() { return age; }
    @Override
    public String getName() { return name; }
    public static void main(String[] args) throws IOException {
        Person person = new PersonImpl("John", 30);
        PersonSkeleton skeleton = new PersonSkeleton(person, 1234);
        skeleton.start();
    }
}

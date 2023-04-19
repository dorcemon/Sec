package RMI.Demo1;

import RMI.Demo1.Person;

public class PersonImpl implements Person {

    private final String name;
    private final int age;

    public PersonImpl(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getAge() {
        return age;
    }
}

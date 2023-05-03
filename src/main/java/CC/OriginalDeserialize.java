package CC;

import java.io.*;

public class OriginalDeserialize {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = "D:\\1.txt";
        Person person = new Person("ceshi", 22);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(person);
        oos.close();
        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis);

        // 漏洞触发点在此处
        Person o = (Person) ois.readObject();
        ois.close();
        System.out.println(o.getClass());
        //因为是重写了readObject()，且没有调用defaultReadObject方法，所以未序列化这几个字段。如果是调用了defaultReadObject方法，那么这几个字段将被序列化，读取的值不为空
        System.out.println(o.name);
        System.out.println(o.age);
    }

    public static class Person implements Serializable {
        private String name;
        private int age;
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        //重写了readObject
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Runtime.getRuntime().exec("calc.exe");
            //注意
            in.defaultReadObject();
        }
    }
}

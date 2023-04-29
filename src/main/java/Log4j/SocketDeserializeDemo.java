package Log4j;

import org.apache.log4j.net.SimpleSocketServer;

/**
 * CVE-2019-17571
 * Log4j1.2.x<=1.2.17
 * ���ʾ��ǶԴ� socket ���л�ȡ������û�н��й��ˣ���ֱ�ӷ����л�
 * SimpleSocketServer.main()
 *      SocketNode.run()
 *          new ObjectInputStream(new BufferedInputStream(socket.getInputStream())).readObject()
 */
public class SocketDeserializeDemo {

    public static void main(String[] args){
        System.out.println("INFO: Log4j Listening on port 8888");
        String[] arguments = {"8888", (new SocketDeserializeDemo()).getClass().getClassLoader().getResource("log4j.properties").getPath()};
        SimpleSocketServer.main(arguments);
        System.out.println("INFO: Log4j output successfuly.");
    }

}

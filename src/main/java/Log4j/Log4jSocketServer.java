package Log4j;

import org.apache.logging.log4j.core.net.server.ObjectInputStreamLogEventBridge;
import org.apache.logging.log4j.core.net.server.TcpSocketServer;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * CVE-2017-5645
 * Log4j 2.x<=2.8.1
 *
 * TcpSocketServer.SocketHandler()
 *      SocketHandler.run()
 *          ObjectInputStreamLogEventBridge.logEvents().readObject()
 */
public class Log4jSocketServer {

    public static void main(String[] args) {
        TcpSocketServer<ObjectInputStream> myServer = null;

        try {
            myServer = new TcpSocketServer<ObjectInputStream>(8888, new ObjectInputStreamLogEventBridge());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        myServer.run();
    }

}

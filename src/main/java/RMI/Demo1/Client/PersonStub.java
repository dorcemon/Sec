package RMI.Demo1.Client;

import RMI.Demo1.Person;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

//实现Person接口，需要getName、age方法
public class PersonStub implements Person {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public PersonStub(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public String getName() {
        try {
            //我要读取getName，所以将getName作为字符串序列化过去
            out.writeObject("getName");
            //自己发送结束后，就等待接收
            out.flush();
            //读取server传过来的对象
            return (String) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getAge() {
        try {
            out.writeObject("getAge");
            out.flush();
            return (int) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

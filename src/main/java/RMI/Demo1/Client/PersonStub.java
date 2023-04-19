package RMI.Demo1.Client;

import RMI.Demo1.Person;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
            out.writeObject("getName");
            out.flush();
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

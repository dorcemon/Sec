package RMI.Demo1.Server;


import RMI.Demo1.Person;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//Skeleton需要传入两个参数，person类和serversocket端口
public class PersonSkeleton extends Thread {
    private final Person person;
    private final ServerSocket serverSocket;

    public PersonSkeleton(Person person, int port) throws IOException {
        this.person = person;
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        while (true) {
            try (Socket socket = serverSocket.accept();
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                String methodName = (String) in.readObject();
                switch (methodName) {
                    case "getName":
                        out.writeObject(person.getName());
                        out.flush();
                    case "getAge":
                        out.writeObject(person.getAge());
                        out.flush();
                    default:
                        out.writeObject(null);
                        out.flush();
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}



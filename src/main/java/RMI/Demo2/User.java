package RMI.Demo2;

import java.io.Serializable;

//注意要实现Serializable接口，那么demo1中为什么没有实现serializable接口
public class User implements Serializable {

    private int userId;
    private String username;
    private int age;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", age=" + age +
                '}';
    }
}

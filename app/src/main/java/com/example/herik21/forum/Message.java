package com.example.herik21.forum;

/**
 * Created by Herik21 on 11/10/2016.
 */
public class Message {

    public int id;
    public String content;
    public String user;
    public String timestamp;

    public Message(){}

    public Message(int id, String content, String user, String timestamp) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

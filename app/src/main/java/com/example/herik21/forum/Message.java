package com.example.herik21.forum;

/**
 * Created by Herik21 on 11/10/2016.
 */
public class Message {

    private int threadId;
    private String content;
    private String user;
    private String timestamp;
    private String photoUrl;

    public Message(){}

    public Message(int id, String content, String user, String timestamp, String photoUrl) {
        this.threadId = id;
        this.content = content;
        this.user = user;
        this.timestamp = timestamp;
        this.photoUrl = photoUrl;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

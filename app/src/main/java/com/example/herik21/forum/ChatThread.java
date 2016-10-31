package com.example.herik21.forum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Herik21 on 29/10/2016.
 */

public class ChatThread {
    public String title;
    public String description;
    public String threadId;
    public Map<String,Object> users;

    public ChatThread(){}

    public ChatThread(String title, String desc, String key){
        this.title = title;
        this.description = desc;
        this.threadId = key;
        this.users = users;
    }

    public ChatThread(String title, String desc, String key, HashMap<String,Object> users){
        this.title = title;
        this.description = desc;
        this.threadId = key;
        this.users = users;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("description", description);
        if(users!=null) {
            result.put("users", users);
        }
        return result;
    }
}

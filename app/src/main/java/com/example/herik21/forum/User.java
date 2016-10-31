package com.example.herik21.forum;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Herik21 on 29/10/2016.
 */

public class User {

    public String idUsuario;
    public String Username;
    public String Nickname;
    public String PhotoURL;
    public String key;
    public Boolean notificacion = false;
    //public List<ChatThread> threads;

    public User (){}

    public User (String idUsuario,String Username, String Nickname,String PhotoURL,String key){
        this.idUsuario=idUsuario;
        this.Username=Username;
        this.Nickname=Nickname;
        this.PhotoURL=PhotoURL;
        this.key=key;
        //this.threads = Collections.emptyList();
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("idUsuario", idUsuario);
        result.put("Username", Username);
        result.put("Nickname", Nickname);
        result.put("PhotoURL", PhotoURL);
        result.put("key",key);
        return result;
    }

}


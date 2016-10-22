package com.example.herik21.forum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    public ListView messageList;
    public ArrayList<Message> messages;
    public MessageAdapter cAdapter;
    public EditText msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageList = (ListView)findViewById(R.id.messages);
        /// get messages from Firebase
        messages = new ArrayList<>();
        ///
        cAdapter = new MessageAdapter(this,messages);
        messageList.setAdapter(cAdapter);
        msg = (EditText)findViewById(R.id.content);
    }

    public void onSend(View v){
        String content = msg.getText().toString();
        String user = "currentUser";
        String timestamp = getNow();
        Message nMsg = new Message(0,content,user,timestamp);
        messages.add(nMsg);
        cAdapter.setData(messages);
        messageList.setAdapter(cAdapter);
        /// Send to Firebase

        ///
    }
    public static String getNow() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%d-%m-%Y %T");
        return sTime;
    }
}

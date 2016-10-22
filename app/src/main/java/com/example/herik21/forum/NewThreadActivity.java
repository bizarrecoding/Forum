package com.example.herik21.forum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class NewThreadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_thread);
    }

    public void onClickAdd(View view){
        EditText newUser = (EditText)findViewById(R.id.newUser);
        String user = newUser.getText().toString();
    }
    public void onClickCreate(View view){
        EditText newThread = (EditText)findViewById(R.id.newThread);
        String thread = newThread.getText().toString();
        finish();
    }
}

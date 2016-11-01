package com.example.herik21.forum;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    /*private static final String MESSAGES_CHILD = "messages";
    /*public ListView messageList;
    public ArrayList<Message> messages;
    public MessageAdapter cAdapter;
    */
    public EditText msg;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private DatabaseReference messageTable;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private String mUsername;
    private String mPhotoUrl;
    private String mThreadID;
    private ImageButton Send;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent parent = getIntent();
        mUsername = parent.getStringExtra("user");
        mPhotoUrl = parent.getStringExtra("profilepic");
        mThreadID = parent.getStringExtra("threadId");
        mTitle = parent.getStringExtra("title");
        FirebaseMessaging.getInstance().subscribeToTopic(mTitle.replace(" ","_"));
        getSupportActionBar().setTitle(mTitle);
        msg = (EditText)findViewById(R.id.content);
        Send = (ImageButton)findViewById(R.id.Send);
        //Toast.makeText(this,"thread id: "+mThreadID,Toast.LENGTH_SHORT).show();

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        messageTable = FirebaseDatabase.getInstance().getReference();
        Query messageRef = messageTable.child("messages").orderByChild("threadId").equalTo(mThreadID);

        Log.d("MSGs","query: id equal to"+mThreadID);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.message,
                MessageViewHolder.class,
                messageRef ){

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              Message newMessage, int position) {
                //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.content.setText(newMessage.getContent());
                viewHolder.user.setText(newMessage.getUser());
                String[] time = newMessage.getTimestamp().split(" ");
                if(time.length == 2) {
                    viewHolder.time.setText(time[0]);
                    viewHolder.time2.setText(time[1]);
                }else{
                    viewHolder.time.setText(time[0]);
                    viewHolder.time2.setVisibility(View.INVISIBLE);
                }
                if (newMessage.getPhotoUrl() == null) {
                    viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ChatActivity.this)
                        .load(newMessage.getPhotoUrl())
                        .into(viewHolder.icon);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int messageCount = mFirebaseAdapter.getItemCount();
                //Log.d("MSGs","msg count: "+messageCount+" on thread "+mThreadID);
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }

        });
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    Send.setEnabled(true);
                } else {
                    Send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(msg.getText().toString().trim().length()>0 ){
                    Message newMessage = new Message(
                            mThreadID,
                            msg.getText().toString(),
                            mUsername,
                            getNow(),
                            mPhotoUrl);
                    //messageTable;
                    String key = messageTable.child("messages").push().getKey();
                    messageTable.child("messages").child(key).setValue(newMessage);
                    msg.setText("");

                    FirebaseMessaging fm = FirebaseMessaging.getInstance();
                    Log.d("FCM","about to send");
                    new PostTask().execute();
                    fm.send(new RemoteMessage.Builder("966970890362" + "@gcm.googleapis.com")
                            .setMessageId(key)//Integer.toString(msgId.incrementAndGet()))
                            .addData("my_message", "Hello World")
                            .build());

                }
            }
        });
    }

    public static String getNow() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%d-%m-%Y %T");
        return sTime.substring(0,sTime.length()-3);
    }

    private class PostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization","key=AIzaSyDDrDOsQ_ksfdlsUBdi9P5HtzORlwL-EM8");
                conn.setRequestProperty("Content-Type","application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("to","/topics/"+mTitle.replace(" ","_"));
                JSONObject info = new JSONObject();
                info.put("title", "Greendit");
                info.put("body", "New message in "+mTitle);
                json.put("notification", info);

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(json.toString());
                wr.flush();
                conn.getInputStream();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}

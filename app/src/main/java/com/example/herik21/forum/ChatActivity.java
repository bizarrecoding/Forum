package com.example.herik21.forum;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private static final String MESSAGES_CHILD = "messages";
    public ListView messageList;
    public ArrayList<Message> messages;
    public MessageAdapter cAdapter;
    public EditText msg;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;
    private String mUsername;
    private String mPhotoUrl;
    private int mThreadID;
    private ImageButton Send;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent parent = getIntent();
        mUsername = parent.getStringExtra("user");
        mPhotoUrl = parent.getStringExtra("profilepic");
        mThreadID = Integer.parseInt(parent.getStringExtra("threadId"));
        mTitle = parent.getStringExtra("title");

        getSupportActionBar().setTitle(mTitle);
        msg = (EditText)findViewById(R.id.content);
        Send = (ImageButton)findViewById(R.id.Send);
        Toast.makeText(this,"thread id: "+mThreadID,Toast.LENGTH_SHORT).show();

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query messageRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD).orderByChild("id").equalTo(mThreadID);

        Log.d("MSGs","query: id equal to"+mThreadID);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.message,
                MessageViewHolder.class,
                messageRef ){        //mFirebaseDatabaseReference.child(MESSAGES_CHILD)) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              Message newMessage, int position) {
                //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.content.setText(newMessage.getContent());
                viewHolder.user.setText(newMessage.getUser());
                viewHolder.time.setText(newMessage.getTimestamp());
                //Log.d("MSGs","message threadid:"+newMessage.getThreadId());
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
                Message newMessage = new Message(
                        mThreadID,
                        msg.getText().toString(),
                        mUsername,
                        getNow(),
                        mPhotoUrl);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD)
                        .push().setValue(newMessage);
                msg.setText("");
            }
        });
    }

    public static String getNow() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%d-%m-%Y");
        return sTime;
    }
}

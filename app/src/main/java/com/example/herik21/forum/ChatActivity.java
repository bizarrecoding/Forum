package com.example.herik21.forum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChatActivity extends AppCompatActivity {

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
    private final int PIC_REQUEST = 1;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(mTitle.equals("Main thread")) {
            getSupportActionBar().setTitle(R.string.main_title);
        }else{
            getSupportActionBar().setTitle(mTitle);
        }
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
            protected void populateViewHolder(final MessageViewHolder viewHolder,
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
                String src = newMessage.getContent();
                if(src.length()>5 && src.substring(0,5).equals("$img/")){
                    try {
                        final String imgref = src.substring(5, src.length());
                        Log.d("img",imgref);
                        StorageReference storageRef = FirebaseStorage.getInstance()
                                .getReferenceFromUrl("gs://forum-1b336.appspot.com");
                        viewHolder.image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(ChatActivity.this,ImageActivity.class);
                                i.putExtra("image",imgref);
                                startActivity(i);
                            }
                        });
                        Drawable myIcon = getResources().getDrawable( R.drawable.message_alert );
                        viewHolder.image.setImageDrawable(myIcon);
                        viewHolder.image.setVisibility(View.VISIBLE);
                        viewHolder.content.setVisibility(View.INVISIBLE);
                    }catch (Exception e){
                        Log.e("Error",e.getMessage()+"\n"+e.getCause().toString());
                    }
                }else{
                    viewHolder.content.setText(newMessage.getContent());
                    viewHolder.image.setVisibility(View.INVISIBLE);
                    Drawable myIcon = getResources().getDrawable( R.drawable.message_alert );
                    viewHolder.image.setImageDrawable(myIcon);
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
                    sendToFirebase(msg.getText().toString());
                }
            }
        });

        ImageButton pic = (ImageButton)findViewById(R.id.pic);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.setType("image/*");
                startActivityForResult(i, PIC_REQUEST);
            }
        });
    }

    protected void sendToFirebase(String content){
        Message newMessage = new Message(
                mThreadID,
                content,
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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PIC_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    if (data != null) {
                        Uri selectedImage = data.getData();
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap pic = BitmapFactory.decodeStream(imageStream);
                        uploadImage(pic);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void uploadImage(Bitmap picture){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://forum-1b336.appspot.com");
        Long tsLong = System.currentTimeMillis()/1000;
        final String ts = tsLong.toString();
        StorageReference picRef = storageRef.child(ts+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = picRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(ChatActivity.this,exception.getMessage(),Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d("img",downloadUrl.toString());
                Toast.makeText(ChatActivity.this,downloadUrl.getHost()+""+downloadUrl.getPath(),Toast.LENGTH_LONG).show();
                //sendToFirebase("$img/"+downloadUrl.toString());
                sendToFirebase("$img/"+ts+".jpg");
            }
        });
    }

    public static String getNow() {
        Time now = new Time();
        now.setToNow();
        String sTime = now.format("%d-%m-%Y %T");
        return sTime.substring(0,sTime.length()-3);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!mThreadID.equals("-1")) {
            getMenuInflater().inflate(R.menu.chat_menu, menu);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent i = new Intent(this,addUserToChat.class);
                i.putExtra("key",mThreadID);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}

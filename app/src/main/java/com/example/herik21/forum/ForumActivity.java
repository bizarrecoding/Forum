package com.example.herik21.forum;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.fabric.sdk.android.Fabric;


public class ForumActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "BFUMc1FPk7r0nDDz0uRpTz8Ad";
    private static final String TWITTER_SECRET = "BSlqtAYlMPToweucA2c6wT5q367OyH31FI9nl82z0e6oXFTll3";

    ThreadAdapter threadAdapter;
    private ListView lv;
    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private String mDisplayname = "Anonymous";
    private String mPhotoUrl;
    public ArrayList<ChatThread> threads;
    private String ctuserkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_forum);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
            final Query ctuser = userTable.orderByChild("idUsuario").equalTo(mFirebaseUser.getUid()).limitToFirst(1);
            ctuser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Log.d("user",snapshot.toString());
                    if (snapshot.getValue() == null) {
                        //user exists, do something
                        Log.d("New user", mFirebaseUser.getUid());
                        String username = mFirebaseUser.getEmail();
                        String userid = mFirebaseUser.getUid();
                        String photoUrl = mFirebaseUser.getPhotoUrl().toString();
                        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
                        String key = userTable.push().getKey();
                        Log.d("New user", key + " registered");
                        User user = new User(userid, username, mDisplayname, photoUrl, key);
                        userTable.child(key).setValue(user);
                        //Toast.makeText(ForumActivity.this,"User: "+key+" registered",Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(ForumActivity.this,"User: "+snapshot.getValue()+"is already registered",Toast.LENGTH_SHORT).show();
                        for (DataSnapshot user : snapshot.getChildren()) {
                            Map<String, Object> u = (Map) user.getValue();
                            ctuserkey = u.get("key").toString();
                            for (String key : u.keySet()) {
                                Log.d(key, u.get(key).toString());
                            }
                            mDisplayname = u.get("Nickname").toString();
                            ForumActivity.this.getSupportActionBar().setTitle(mDisplayname);
                            Toast.makeText(ForumActivity.this,"User nickname: "+u.get("Nickname").toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ForumActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            String tempDisplayname = mFirebaseUser.getDisplayName();
            getSupportActionBar().setTitle(tempDisplayname);
            ImageView userprofile = (ImageView) findViewById(R.id.usericon);

            Glide.with(ForumActivity.this)
                    .load(mFirebaseUser.getPhotoUrl())
                    .into(userprofile);
            Snackbar.make(findViewById(R.id.textView4), "Welcome, " + tempDisplayname, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            threads =  new ArrayList<>();
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        Query query = FirebaseDatabase.getInstance().getReference().child("Threads");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                threads.clear();
                for(DataSnapshot th : snapshot.getChildren()){
                    String title = th.child("title").getValue(String.class);
                    String description = th.child("description").getValue(String.class);
                    String threadId = th.child("threadId").getValue(String.class);
                    Boolean member = th.child("users").toString().contains(mFirebaseUser.getUid());
                    if(member){
                        ChatThread cth = new ChatThread(title,description,threadId);
                        threads.add(cth);
                        Log.d("member present in", title+" - "+threadId);
                    }
                }
                threadAdapter.notifyDataSetChanged();
                for(ChatThread cth : threads){
                    FirebaseMessaging.getInstance().subscribeToTopic(cth.title.replace(" ","_"));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ForumActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        lv = (ListView) findViewById(R.id.list);
        View mainThreadView = findViewById(R.id.main);
        mainThreadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openThread("Main thread","-1");
            }
        });
        try {
            ((TextView) mainThreadView.findViewById(R.id.title)).setText("Greendit Discussion");
            ((TextView) mainThreadView.findViewById(R.id.desc)).setText("Description");
        }catch (Exception e){

        }
        threadAdapter = new ThreadAdapter(this, threads);
        lv.setAdapter(threadAdapter);
        lv.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            openThread(((TextView)view.findViewById(R.id.tvThreadtitle)).getText().toString(),((ChatThread)threadAdapter.getItem(position)).threadId);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //create new thread
                Intent i = new Intent(ForumActivity.this, NewThreadActivity.class);
                //i.putExtra("key",ctuserkey);
                i.putExtra("uid",mFirebaseUser.getUid());
                startActivity(i);
            }
        });
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mDisplayname = "Anonymous";
                startActivity(new Intent(this, SignInActivity.class));
                for(ChatThread cth : threads){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(cth.title.replace(" ","_"));
                }
                return true;
            case R.id.edit_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Edit Display Name");
                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDisplayname = input.getText().toString();
                        if (input.getText().toString().trim().length() > 0) {
                            DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
                            Map<String, Object> uChildUpdates = new HashMap<>();
                            uChildUpdates.put("Nickname", mDisplayname);
                            userTable.child(ctuserkey).updateChildren(uChildUpdates);
                            getSupportActionBar().setTitle(mDisplayname);
                            Toast.makeText(ForumActivity.this, "New Display Name: "+mDisplayname, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForumActivity.this, "DisplayName no debe ser vacio"+mDisplayname, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("none", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    public void openThread(String title, String id){
        Log.d("ForumActivity th ", "Click on: " + id);
        Intent i = new Intent(ForumActivity.this, ChatActivity.class);
        i.putExtra("user", mDisplayname);
        i.putExtra("profilepic",mPhotoUrl);
        i.putExtra("title",title);
        //TODO: put the true id of the thread
        i.putExtra("threadId",""+id);
        startActivity(i);
    }
}

package com.example.herik21.forum;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
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


public class ForumActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener {

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
    private static int lock = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main_forum);
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        //Material Design Widgets
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            final ImageView userprofile = (ImageView) findViewById(R.id.usericon);
            String tempDisplayname = mFirebaseUser.getDisplayName();
            //getSupportActionBar().setTitle(tempDisplayname);
            final DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
            final Query ctuser = userTable.orderByChild("idUsuario").equalTo(mFirebaseUser.getUid()).limitToFirst(1);
            ctuser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) {
                        //user exists, do something
                        Log.d("New user", mFirebaseUser.getUid());
                        mDisplayname = mFirebaseUser.getDisplayName();
                        String username = mFirebaseUser.getEmail();
                        String userid = mFirebaseUser.getUid();
                        String photoUrl = mFirebaseUser.getPhotoUrl().toString();
                        DatabaseReference userTable = FirebaseDatabase.getInstance().getReference().child("Users");
                        String key = userTable.push().getKey();
                        Log.d("New user", key + " registered");
                        User user = new User(userid, username, mDisplayname, photoUrl, key);
                        userTable.child(key).setValue(user);
                    } else {
                        for (DataSnapshot user : snapshot.getChildren()) {
                            Map<String, Object> u = (Map) user.getValue();
                            Map<String, Object> update = new HashMap<>();
                            update.put("PhotoURL",mFirebaseUser.getPhotoUrl().toString());
                            userTable.child(u.get("key").toString()).updateChildren(update);
                            ctuserkey = u.get("key").toString();
                            mDisplayname = u.get("Nickname").toString();
                            String mPhoto = u.get("PhotoURL").toString();
                            navigationView.getMenu().getItem(0).setChecked(Boolean.parseBoolean(u.get("notificacion").toString()));
                            for (String key : u.keySet()) {
                                Log.d(key, u.get(key).toString());
                            }
                            Log.d("UI","Should update");
                            getSupportActionBar().setTitle(mDisplayname);
                            ForumActivity.this.getSupportActionBar().setTitle(mDisplayname);
                            Glide.with(ForumActivity.this)
                                    .load(u.get("PhotoURL").toString())
                                    .into(userprofile);
                            //Toast.makeText(ForumActivity.this,"User nickname: "+u.get("Nickname").toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(lock == 1){
                        Snackbar.make(findViewById(R.id.nestedscroll), "Welcome, " + mDisplayname, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        lock = 0;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ForumActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("photourl",mFirebaseUser.getPhotoUrl().toString());
            if(!mDisplayname.equals("Anonymous")) {
                getSupportActionBar().setTitle(mDisplayname);
            }else{
                getSupportActionBar().setTitle(mFirebaseUser.getDisplayName());
            }
            Glide.with(ForumActivity.this)
                .load(mFirebaseUser.getPhotoUrl())
                .into(userprofile);

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
            ((TextView) mainThreadView.findViewById(R.id.title)).setText(R.string.Greendit_Discussion);
            ((TextView) mainThreadView.findViewById(R.id.desc)).setText(R.string.description);
        }catch (Exception e){
            Log.e("Error",e.getMessage());
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
                i.putExtra("uid",mFirebaseUser.getUid());
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        switch (item.getItemId()) {
            case R.id.nav_notification:
                if(item.isChecked()) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("Main_thread");
                    item.setChecked(false);
                }else{
                    FirebaseMessaging.getInstance().subscribeToTopic("Main_thread");
                    item.setChecked(true);
                }
                Map<String,Object> update = new HashMap<>();
                update.put("notificacion",item.isChecked());
                DatabaseReference userTable =  FirebaseDatabase.getInstance().getReference().child("Users");
                userTable.child(ctuserkey).updateChildren(update);

                break;
            case R.id.nav_editname:
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                        .setTitle(R.string.edit_dname)
                        .setView(input)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_about:
                startActivity(new Intent(this,AboutActivity.class));
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_exit:
                //mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mDisplayname = "Anonymous";
                for(ChatThread cth : threads){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(cth.title.replace(" ","_"));
                }
                startActivity(new Intent(this, SignInActivity.class));
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
        return true;
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}

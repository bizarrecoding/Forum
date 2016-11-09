package com.example.herik21.forum;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        final ImageView image = (ImageView)findViewById(R.id.imageref);
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReferenceFromUrl("gs://forum-1b336.appspot.com");
        String imgref = getIntent().getStringExtra("image");

        storageRef.child(imgref).getDownloadUrl().addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                Uri url = (Uri)o;
                Glide.with(ImageActivity.this)
                        .load(url.toString())
                        .into(image);
            }
        });
    }
}

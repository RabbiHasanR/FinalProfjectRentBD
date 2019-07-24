package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Adapter.ImageSliderAdapter;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostDetailsActivity extends AppCompatActivity {

    private String key=null;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseReference;
    private Post post;

    @BindView(R.id.rent)
    TextView rentTxt;
    @BindView(R.id.title_txt)
    TextView titleTxt;
    @BindView(R.id.availableDate)
    TextView availableDateTxt;
    @BindView(R.id.address)
    TextView addressTxt;
    @BindView(R.id.phone)
    TextView phoneTxt;
    @BindView(R.id.description)
    TextView descriptionTxt;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        post=new Post();
        ButterKnife.bind(this);
        Intent intent=getIntent();
        if(intent.hasExtra("key")){
            key=intent.getExtras().getString("key");
            Toast.makeText(this, intent.getExtras().toString(), Toast.LENGTH_SHORT).show();
        }
        retrivePostPhoto();
        retrivePostData();
    }

    /**
     * retrive post data from firebase datbase
     */

    private void retrivePostData(){
        DatabaseReference mDatabaseReference= database.getReference().child("Posts").child(key);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    post = dataSnapshot.getValue(Post.class);
//                                                Log.d("User:",user.getUsername());
                    if(post!=null){
                        setDataOnTxtView(post);
                    }
                    else {
                        Toast.makeText(PostDetailsActivity.this, "User value is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * retrive photo download url from firebase database
     */
    private void retrivePostPhoto(){
        List<Photo> photos=new ArrayList<>();
        DatabaseReference myRef2= database.getReference().child("PostPhotos");
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot2:dataSnapshot.child(key).getChildren()){
                    Photo photo=dataSnapshot2.getValue(Photo.class);
                    photos.add(photo);
                }
                if(!photos.isEmpty()){
                    ImageSliderAdapter imageAdapter=new ImageSliderAdapter(PostDetailsActivity.this,photos);
                    viewPager.setAdapter(imageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setDataOnTxtView(Post post){
        titleTxt.setText(post.getTitle());
        rentTxt.setText(post.getRent());
        availableDateTxt.setText(post.getAvailableDate());
        addressTxt.setText(post.getAddress());
        phoneTxt.setText(post.getMobileNumber());
        descriptionTxt.setText(post.getDescription());
    }
}

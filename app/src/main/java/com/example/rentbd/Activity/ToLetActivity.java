package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Adapter.PostAdapter;
import com.example.rentbd.Adapter.SliderAdapter;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ToLetActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private List<Post> posts = new ArrayList<>();
    private String type;
//    private DatabaseReference myRef2;

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyTextView;
    //    @BindView(R.id.imageSlider)
    private SliderView sliderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_let);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Posts");
        ButterKnife.bind(this);
        Intent intent=getIntent();
        if(intent.hasExtra("type")){
            type=intent.getExtras().getString("type");
            setPostOnRecyclerview();
        }
    }

    /**
     * set post on recyclerview
     */
    private void setPostOnRecyclerview(){
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    Post post = dataSnapshot1.getValue(Post.class);
                    /**
                     * add post based on type
                     */
                    if(post.getType().equalsIgnoreCase(type)){
                        posts.add(post);
                        retrivePhotos(post.getKey());
                    }
                }
                Log.d("Posts:",String.valueOf(posts.isEmpty()));
                if(posts.isEmpty()){
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.INVISIBLE);
                    PostAdapter recycler = new PostAdapter(posts,ToLetActivity.this);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ToLetActivity.this);
                    recyclerView.setLayoutManager(layoutmanager);
                    recyclerView.setItemAnimator( new DefaultItemAnimator());
                    recyclerView.setAdapter(recycler);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ToLetActivity.this, "Failed to retrive data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * retrive photos download url from firebase database
     * @param userId
     */
    private void retrivePhotos(String userId){
        List<Photo> photos=new ArrayList<>();
        DatabaseReference myRef2= database.getReference().child("Photos");
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot2:dataSnapshot.child(userId).getChildren()){
                    Photo photo=dataSnapshot2.getValue(Photo.class);
                    photos.add(photo);
                }
                if(!photos.isEmpty()){
                    sliderView=findViewById(R.id.imageSlider);
                    final SliderAdapter adapter = new SliderAdapter(photos);
                    Log.d("Size:", String.valueOf(photos.size()));
                    sliderView.setSliderAdapter(adapter);
                    sliderView.setIndicatorAnimation(IndicatorAnimations.SLIDE); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                    sliderView.setSliderTransformAnimation(SliderAnimations.CUBEINROTATIONTRANSFORMATION);
                    sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                    sliderView.setIndicatorSelectedColor(Color.WHITE);
                    sliderView.setIndicatorUnselectedColor(Color.GRAY);
                    sliderView.startAutoCycle();

                    sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
                        @Override
                        public void onIndicatorClicked(int position) {
                            sliderView.setCurrentPagePosition(position);
                            Toast.makeText(ToLetActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * move to post activity
     */
    private void movePostActivity(){
        Intent intent=new Intent(this,PostActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.fab)
    void onClickFab(){
        movePostActivity();
    }
}

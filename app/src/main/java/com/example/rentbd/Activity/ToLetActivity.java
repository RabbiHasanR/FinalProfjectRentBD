package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private FirebaseAuth mAuth;
    private List<Post> posts = new ArrayList<>();
    private String type;
    boolean nearby=false;
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
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Posts");
        ButterKnife.bind(this);
        Intent intent=getIntent();
        if(intent.hasExtra("type")){
            type=intent.getExtras().getString("type");
            setPostOnRecyclerview();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if(nearby==false){
            MenuItem nearby = menu.findItem(R.id.action_nearby);
            nearby.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show();
            return true;
        }
        else if(id==R.id.action_logout){
            if(mAuth.getCurrentUser()!=null){
                mAuth.signOut();
                moveLoginActivity();
            }
            return true;

        }
        else if(id==R.id.action_about){
            Toast.makeText(this, "Under Construction", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveLoginActivity(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
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
                    }
                }
                Log.d("Posts:",String.valueOf(posts.isEmpty()));
                if(posts.isEmpty()){
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyTextView.setVisibility(View.VISIBLE);
                }
                else {
                    boolean isDelate=false;
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyTextView.setVisibility(View.INVISIBLE);
                    PostAdapter recycler = new PostAdapter(posts,ToLetActivity.this,isDelate);
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(ToLetActivity.this);
                    recyclerView.setLayoutManager(layoutmanager);
                    recyclerView.setItemAnimator( new DefaultItemAnimator());
                    recyclerView.setAdapter(recycler);
                    recycler.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ToLetActivity.this, "Failed to retrive data", Toast.LENGTH_SHORT).show();
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

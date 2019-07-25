package com.example.rentbd.Fragment;


import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Activity.ToLetActivity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class UserPostFragment extends Fragment {

    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private List<Post> posts = new ArrayList<>();
    View view;

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.empty_view)
    TextView emptyTextView;

    public UserPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_user_post, container, false);
        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Posts");
        ButterKnife.bind(this,view);
        retrivePost();
        // Inflate the layout for this fragment
        return view;
    }

    private void retrivePost(){
        String userId=mAuth.getCurrentUser().getUid();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // StringBuffer stringbuffer = new StringBuffer();
                for(DataSnapshot dataSnapshot1 :dataSnapshot.getChildren()){

                    Post post = dataSnapshot1.getValue(Post.class);
                    /**
                     * add post based on type
                     */
                    if(post.getUserId().equalsIgnoreCase(userId)){
                        posts.add(post);
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
                    PostAdapter recycler = new PostAdapter(posts, getContext());
                    RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(layoutmanager);
                    recyclerView.setItemAnimator( new DefaultItemAnimator());
                    recyclerView.setAdapter(recycler);
                    recycler.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to retrive data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.rentbd.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentbd.Activity.PostDetailsActivity;
import com.example.rentbd.Activity.ShowInMapActivity;
import com.example.rentbd.Activity.ToLetActivity;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    private List<Post> posts;
    private Context context;
    boolean isDelete;

    public PostAdapter(List<Post> posts,Context context,boolean isDelete) {
        this.posts=posts;
        this.context=context;
        this.isDelete=isDelete;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(MyHolder holder, int position) {
        Post post = posts.get(position);
        retrivePhotos(post.getKey(),holder.itemView);
        holder.titleTxt.setText(post.getTitle());
        holder.addressTxt.setText(post.getAddress());
        holder.phoneTxt.setText(post.getMobileNumber());
        holder.rentTxt.setText(post.getRent());
        holder.availableDateTxt.setText(post.getAvailableDate());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context.getApplicationContext(), PostDetailsActivity.class);
                intent.putExtra("key",post.getKey());
                context.startActivity(intent);
            }
        });
        holder.showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context.getApplicationContext(), ShowInMapActivity.class);
                intent.putExtra("key",post.getKey());
                context.startActivity(intent);
            }
        });
        /*
        delete is not working..Todo
         */
        if(isDelete==true){
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference mDatabaseReference= database.getReference().child("Posts").child(post.getKey());
                    mDatabaseReference. removeValue();
                }
            });
        }

    }

    /**
     * retrive photos download url from firebase database
     * @param key
     */
    private void retrivePhotos(String key,View view){
        List<Photo> photos=new ArrayList<>();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef2= database.getReference().child("PostPhotos");
        myRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot2:dataSnapshot.child(key).getChildren()){
                    Photo photo=dataSnapshot2.getValue(Photo.class);
                    photos.add(photo);
                }
                if(!photos.isEmpty()){
                    final SliderView sliderView=view.findViewById(R.id.imageSlider);
                    final SliderAdapter adapter = new SliderAdapter(photos);
                    Log.d("Size:", String.valueOf(photos.size()));
                    sliderView.setSliderAdapter(adapter);
                    adapter.notifyDataSetChanged();
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
                            Toast.makeText(context, String.valueOf(position), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.post_title)
        TextView titleTxt;
        @BindView(R.id.post_address)
        TextView addressTxt;
        @BindView(R.id.post_phone)
        TextView phoneTxt;
        @BindView(R.id.post_rent)
        TextView rentTxt;
        @BindView(R.id.post_available_date)
        TextView availableDateTxt;
        @BindView(R.id.show_map_btn)
        Button showBtn;
        @BindView(R.id.delete_btn)
        Button deleteBtn;


        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }

}

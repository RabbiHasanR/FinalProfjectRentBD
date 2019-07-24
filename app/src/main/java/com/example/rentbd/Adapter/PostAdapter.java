package com.example.rentbd.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rentbd.Activity.PostDetailsActivity;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder>{

    private List<Post> posts;
    private Context context;

    public PostAdapter(List<Post> posts,Context context) {
        this.posts=posts;
        this.context=context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item,parent,false);

        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }


    public void onBindViewHolder(MyHolder holder, int position) {
        Post post = posts.get(position);
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


        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }

}

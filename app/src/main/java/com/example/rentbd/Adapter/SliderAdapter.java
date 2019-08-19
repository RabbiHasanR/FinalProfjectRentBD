package com.example.rentbd.Adapter;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

public class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH>{

    private List<Photo> photos;

    public SliderAdapter(List<Photo> photos) {
        this.photos=photos;
    }


    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, final int position) {
        Photo photo;
        Uri uri;


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "This is item in position " + position, Toast.LENGTH_SHORT).show();
            }
        });


        switch (position) {
            case 0:
                photo=photos.get(position);
                uri=Uri.parse(photo.getUri());
                viewHolder.textViewDescription.setText(String.valueOf(position));
                viewHolder.textViewDescription.setTextSize(16);
                viewHolder.textViewDescription.setTextColor(Color.WHITE);
                Glide.with(viewHolder.itemView)
                        .load(uri)
                        .centerCrop()
                        .into(viewHolder.imageViewBackground);
                break;
            case 1:
                photo=photos.get(position);
                uri=Uri.parse(photo.getUri());
                viewHolder.textViewDescription.setText(String.valueOf(position));
                viewHolder.textViewDescription.setTextSize(16);
                viewHolder.textViewDescription.setTextColor(Color.WHITE);
                Glide.with(viewHolder.itemView)
                        .load(uri)
                        .centerCrop()
                        .into(viewHolder.imageViewBackground);
                break;
            case 2:
                photo=photos.get(position);
                uri=Uri.parse(photo.getUri());
                viewHolder.textViewDescription.setText(String.valueOf(position));
                viewHolder.textViewDescription.setTextSize(16);
                viewHolder.textViewDescription.setTextColor(Color.WHITE);
                Glide.with(viewHolder.itemView)
                        .load(uri)
                        .centerCrop()
                        .into(viewHolder.imageViewBackground);
                break;
            case 3:
                photo=photos.get(position);
                uri=Uri.parse(photo.getUri());
                viewHolder.textViewDescription.setText(String.valueOf(position));
                viewHolder.textViewDescription.setTextSize(16);
                viewHolder.textViewDescription.setTextColor(Color.WHITE);
                Glide.with(viewHolder.itemView)
                        .load(uri)
                        .centerCrop()
                        .into(viewHolder.imageViewBackground);
                break;
            case 4:
                photo=photos.get(position);
                uri= Uri.parse(photo.getUri());
                viewHolder.textViewDescription.setText(String.valueOf(position));
                viewHolder.textViewDescription.setTextSize(16);
                viewHolder.textViewDescription.setTextColor(Color.WHITE);
                Glide.with(viewHolder.itemView)
                        .load(uri)
                        .centerCrop()
                        .into(viewHolder.imageViewBackground);
                break;



        }

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return photos.size();
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_image_slider_position);
            this.itemView = itemView;
        }
    }
}

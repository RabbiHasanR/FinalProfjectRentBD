package com.example.rentbd.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.R;

import java.util.List;

public class ImageSliderAdapter extends PagerAdapter {

    private Context context;
    private List<Photo> photos;
    LayoutInflater mLayoutInflater;

    public ImageSliderAdapter(Context context,List<Photo> photos){
        this.context=context;
        this.photos=photos;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.viepage_photo_item, container, false);
        final ImageView ivPhoto = (ImageView) itemView.findViewById(R.id.postImage);
        final TextView currentPositonTxt = (TextView) itemView.findViewById(R.id.curent_photo_number);
        final TextView totalPhotoNumberTxt = (TextView) itemView.findViewById(R.id.total_photo_number);
        int curentNumber=position+1;
        currentPositonTxt.setText(String.valueOf(curentNumber));
        totalPhotoNumberTxt.setText(String.valueOf(photos.size()));
        Photo photo=photos.get(position);
        if(photo!=null){
            Uri uri=Uri.parse(photo.getUri());
            Glide.with(context)
                    .load(uri)
                    .into(ivPhoto);
        }
        container.addView(itemView);
        return itemView;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((FrameLayout) object);
    }
}

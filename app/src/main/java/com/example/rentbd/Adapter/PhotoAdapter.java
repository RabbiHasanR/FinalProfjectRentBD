package com.example.rentbd.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rentbd.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyHolder>{

    private List<Uri> photos;
    private Bitmap yourbitmap;
    private Context context;

    public PhotoAdapter(List<Uri> photos, Context context) {
        this.photos=photos;
        this.context=context;
    }

    @Override
    public PhotoAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item,parent,false);

        MyHolder myHolder=new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Uri imageUri=photos.get(position);
        try {
            InputStream iStream = context.getContentResolver().openInputStream(imageUri);
            byte[] b = getBytes(iStream);
            yourbitmap= BitmapFactory.decodeByteArray(b, 0, b.length);
            holder.imageView.setImageBitmap(yourbitmap); //Bitmap.createScaledBitmap(yourbitmap, 120,150, false)
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position=holder.getAdapterPosition();
                photos.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,photos.size());
            }
        });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    @Override
    public int getItemCount() {
        return photos.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.customView)
        ImageView imageView;
        @BindView(R.id.cancel_btn)
        ImageView cancel_btn;

        public MyHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }
    }
}

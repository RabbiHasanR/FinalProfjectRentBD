package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rentbd.Adapter.ViewPagerAdapter;
import com.example.rentbd.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private static final int CAMERA_REQUEST_CODE=1;
    private String userId;
    private boolean isCamera=true;
    private boolean isRetriveImage=false;;
    @BindView(R.id.profile_image)
    ImageView profile_image_iv;
    @BindView(R.id.take_photo)
    ImageView take_photo_iv;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //get firebase authentication instance
        mAuth=FirebaseAuth.getInstance();
        mStorage= FirebaseStorage.getInstance().getReference().child("Photos");
        if(mAuth!=null){
            userId=mAuth.getCurrentUser().getUid();
        }
        ButterKnife.bind(this);
        retriveImageFromFireStore();
        if(!isRetriveImage){
            Glide.with(ProfileActivity.this)
                    .load(R.drawable.noimageicon)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_image_iv);
        }

        clickListnerForCamera();
        // Create an adapter that knows which fragment should be shown on each page
        ViewPagerAdapter adapter = new ViewPagerAdapter(this,getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * retrive image from firebase storage
     */
    private void retriveImageFromFireStore(){
        StorageReference filePath=mStorage.child(userId);
        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                isRetriveImage=true;
                Log.d("Image uri:", String.valueOf(uri));
                Glide.with(ProfileActivity.this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profile_image_iv);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                isRetriveImage=false;
                Toast.makeText(ProfileActivity.this, "Failed retrive Image.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void captureImageFromCamera(){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST_CODE);
        Log.d("Camera:","yes");
    }
    private void getImageFromGallery(){
        isCamera=false;
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,CAMERA_REQUEST_CODE);
    }

    /**
     * upload photo from gallery in firbase storage
     * @param b
     */
    private void uploadPhotoFromGallery(byte[] b){
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading Photo...");
        progressDialog.show();
        StorageReference filePath=mStorage.child(userId); //.child(imageUri.getLastPathSegment())
        filePath.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Uploading Finished", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Upload Failed",Toast.LENGTH_LONG).show();


            }
        });
    }

    /**
     * upload photo from camera in firebase storage
     * @param b
     */
    public void uploadPhotoFromCamera(byte[] b){
        final ProgressDialog progressDialog = new ProgressDialog(ProfileActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Uploading Photo...");
        progressDialog.show();
        StorageReference filePath=mStorage.child(userId);
        filePath.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this, "Uploading Finished", Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ProfileActivity.this,"Upload Failed",Toast.LENGTH_LONG).show();


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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RequestCode:", String.valueOf(requestCode)+resultCode+data);
        if(requestCode==CAMERA_REQUEST_CODE && resultCode != RESULT_CANCELED && data!=null){
            if(isCamera){
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] b = stream.toByteArray();
                circleImage(b);
                uploadPhotoFromCamera(b);
                isCamera=false;
            }
            else {
                Uri imageUri=data.getData();
                try {
                    InputStream iStream =   getContentResolver().openInputStream(imageUri);
                    byte[] b = getBytes(iStream);
                    circleImage(b);
                    uploadPhotoFromGallery(b);
                    isCamera=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }

    /**
     * Create clickListner for take_photo_iv imageview
     */
    private void clickListnerForCamera(){
        take_photo_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    /**
     * Create dialog for chose take photo option
     */
    private void showDialog(){
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a option");

// add a list
        String[] animals = {"Camera", "Gallery"};
        builder.setItems(animals, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case 0:
//                        captureImageFromCamera();
//                        //Toast.makeText(ProfileActivity.this, "Click Camera", Toast.LENGTH_SHORT).show();
//                    case 1:
//                        Toast.makeText(ProfileActivity.this, "Click Gallery", Toast.LENGTH_SHORT).show();
//
//                }
                if(which==0){
                    captureImageFromCamera();
                }
                else {
                    getImageFromGallery();
                    //Toast.makeText(ProfileActivity.this, "Click Gallery", Toast.LENGTH_SHORT).show();
                }
            }
        });

// create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * create circular image view using glide library
     */
    private void circleImage(byte[] imageByte){
        Glide.with(this)
                .asBitmap()
                .load(imageByte)
                .apply(RequestOptions.circleCropTransform())
                .into(profile_image_iv);
    }
}

package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.rentbd.Adapter.PhotoAdapter;
import com.example.rentbd.Model.Photo;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private List<Uri> imagesUriArrayList=new ArrayList<>();
    private final int PICK_IMAGE_MULTIPLE =1;
    private StorageReference mStorageRef;
    //    private List<String> downloadImageUri=new ArrayList<>();
//    int count=0;
    @BindView(R.id.type_spinner)
    Spinner spinner;
    @BindView(R.id.input_date)
    EditText dateEditTxt;
    @BindView(R.id.input_title)
    EditText titleEditTxt;
    @BindView(R.id.input_rent)
    EditText rentEditTxt;
    @BindView(R.id.input_address)
    EditText addressEditTxt;
    @BindView(R.id.input_mobile)
    EditText mobileEditTxt;
    @BindView(R.id.input_description)
    EditText descriptionEditTxt;
    @BindView(R.id.btn_Add_Phots)
    Button addBtn;
    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;


    private FirebaseDatabase database;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReference1;
    private Post post;
    private String type;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        mDatabaseReference= database.getReference().child("Posts");
        mStorageRef= FirebaseStorage.getInstance().getReference().child("PostPhotos");
        post=new Post();
        ButterKnife.bind(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tolet_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }
    private void moveLoginActivity(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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


    /**
     * add photos onclick
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @OnClick(R.id.btn_Add_Phots)
    void OnAddPhotos(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_MULTIPLE); //Intent.createChooser(intent, "Select Picture")
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if(requestCode == PICK_IMAGE_MULTIPLE && data!=null){
                addPhotoInRecyclerView(data);
            }
        }

    }

    /**
     * add photo in recycler view
     * @param data
     */
    private void addPhotoInRecyclerView(Intent data){
        ClipData cd = data.getClipData();
        if(cd==null){
            //imagesUriArrayList.clear();
            imagesUriArrayList.add(data.getData());
        }
        else{
            //if selected data is more then 5 then condition is true
            if (cd.getItemCount() > 5) {
                Snackbar snackbar = Snackbar
                        .make(addBtn, "You can not select more than 5 images", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
//                                        intent.putExtra(Intent.EXTRA_UID,true);
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
                            }
                        });
                snackbar.setActionTextColor(Color.BLUE);
                Toast.makeText(this, "Not select more then 5", Toast.LENGTH_SHORT).show();
                snackbar.show();

            } else {
                // imagesUriArrayList.clear();
                for (int i = 0; i < cd.getItemCount(); i++) {
                    imagesUriArrayList.add(data.getClipData().getItemAt(i).getUri());
                }
            }
        }
        Log.e("SIZE", imagesUriArrayList.size() + "");
        if(imagesUriArrayList.size()>5){
            Toast.makeText(this, "Not select more then 5 photo", Toast.LENGTH_SHORT).show();
            imagesUriArrayList.remove(0);
        }
        else {
            setOnRecyclerview();
        }

    }

    private void setOnRecyclerview(){
        PhotoAdapter recycler = new PhotoAdapter(imagesUriArrayList,this);
        RecyclerView.LayoutManager layoutmanager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutmanager);
        recyclerView.setItemAnimator( new DefaultItemAnimator());
        recyclerView.setAdapter(recycler);
        recycler.notifyDataSetChanged();

    }

    private void storePhoto(String key,String userId){
        StorageReference filePath=mStorageRef.child(key); //.child(imageUri.getLastPathSegment())
        Log.d("key:",key);
        mDatabaseReference1=database.getReference().child("PostPhotos").child(key);
        for(Uri uri:imagesUriArrayList){
            filePath.child(uri.getLastPathSegment()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    StorageReference ref=taskSnapshot.getMetadata().getReference();

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String uriKey=mDatabaseReference1.push().getKey();
                            Photo photo=new Photo();
                            photo.setUri(String.valueOf(uri));
                            photo.setUriKey(uriKey);
                            photo.setUserId(userId);
                            mDatabaseReference1.push().setValue(photo);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this,"Upload Failed",Toast.LENGTH_LONG).show();


                }
            });
        }
        Toast.makeText(PostActivity.this, "Uploading Finished", Toast.LENGTH_SHORT).show();

    }

    /**
     * Store post in firebase database
     */
    private void storePost(){
        mAuth=FirebaseAuth.getInstance();
        String userId=mAuth.getCurrentUser().getUid();
        String key=mDatabaseReference.push().getKey();
        if(!imagesUriArrayList.isEmpty()){
            storePhoto(key,userId);
            imagesUriArrayList.clear();
            setOnRecyclerview();
        }
        String title=titleEditTxt.getText().toString().trim();
        String rent=rentEditTxt.getText().toString().trim();
        String availableDate=dateEditTxt.getText().toString().trim();
        String mobile=mobileEditTxt.getText().toString().trim();
        String address=addressEditTxt.getText().toString().trim();
        String description=descriptionEditTxt.getText().toString().trim();
        Log.d("key",key);
        post.setKey(key);
        post.setAddress(address);
        post.setAvailableDate(availableDate);
        post.setDescription(description);
        post.setMobileNumber(mobile);
        post.setRent(rent);
        post.setTitle(title);
        post.setType(type);
        post.setUserId(userId);
        mDatabaseReference.child(key).setValue(post);
    }

    /**
     * set input field empty
     */
    private void set_input_field_empty(){
        titleEditTxt.setText("");
        addressEditTxt.setText("");
        mobileEditTxt.setText("");
        rentEditTxt.setText("");
        descriptionEditTxt.setText("");
        dateEditTxt.setText("");
        spinner.setSelection(0);

    }

    /**
     * post data in on click
     */
    @OnClick(R.id.btn_post)
    void postOnClick(){
        progressDialog = new ProgressDialog(PostActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
        storePost();
        set_input_field_empty();
        progressDialog.dismiss();
        Toast.makeText(this, "Save post", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.input_date)
    void datePicker(){
        Toast.makeText(this, "Date Picker", Toast.LENGTH_SHORT).show();
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
//        datePickerFragment=DatePickerFragment.newInstance(this);
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Toast.makeText(this, "Selected Type:"+pos, Toast.LENGTH_SHORT).show();
        if(pos==0)
            type="flat";
        else if(pos==1)
            type="mess";
        else if(pos==2)
            type="sublet";
        else if(pos==3)
            type="hostel";
        else if(pos==4)
            type="office";
        else
            type="garage";
    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    /**
     * this is datapicker fragment inner class
     */
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private SimpleDateFormat simpleDateFormat=new SimpleDateFormat("dd-MMMM-YYYY");

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            return  dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            Date date=calendar.getTime();
            Log.d("Date:", String.valueOf(date));
            String dateformate=simpleDateFormat.format(date);
            Log.d("Date Formate:", dateformate);
            ((PostActivity)getActivity()).dateEditTxt.setText(dateformate);

        }
    }

}

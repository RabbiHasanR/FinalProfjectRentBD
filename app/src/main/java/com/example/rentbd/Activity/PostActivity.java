package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PostActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMarkerClickListener{
    private static final String TAG = "PostActivity";
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
    @BindView(R.id.input_latlang)
    EditText latlangEditTxt;
    private MapView mMapView;

    private Marker mMarker;
    private GoogleMap mMap;

    //current location
    private FusedLocationProviderClient mFusedLocationClient;
    private double latitude;
    private double longitude;


    private FirebaseDatabase database;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabaseReference1;
    private DatabaseReference mDatabaseReference4;
    private Post post;
    private String type;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Toolbar toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        mDatabaseReference= database.getReference().child("Posts");
        mDatabaseReference4= database.getReference().child("PostLocations");
        mStorageRef= FirebaseStorage.getInstance().getReference().child("PostPhotos");
        post=new Post();
        ButterKnife.bind(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tolet_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        getLastKnownLocation();
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
        if (!validate()) {
            Toast.makeText(this, "Please give valid input.", Toast.LENGTH_SHORT).show();
            return;
        }
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
        String latLnag=latlangEditTxt.getText().toString();
        String[] separated = latLnag.split(",");
        double latitude=Double.parseDouble(separated[0].trim());
        double longitude=Double.parseDouble(separated[1].trim());
        mDatabaseReference.child(key).setValue(post);
        GeoFire geoFire = new GeoFire(mDatabaseReference4);
        geoFire.setLocation(key,new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });
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
        latlangEditTxt.setText("");

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

    /**get location from user using marker move
     *
     */
    private void setLocation(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Hotel Info");
        //alertDialog.setIcon(R.drawable.action_hotels);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.mapview_layout, null);
        alertDialog.setView(promptView);

        mMapView = promptView.findViewById(R.id.map_view);
        MapsInitializer.initialize(this);

        mMapView.onCreate(alertDialog.onSaveInstanceState());
        mMapView.onResume();


        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if (ActivityCompat.checkSelfPermission(PostActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(PostActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                mMap=googleMap;
                LatLng posisiabsen = new LatLng(latitude, longitude); ////your lat lng
                mMarker=googleMap.addMarker(new MarkerOptions().position(posisiabsen).title("Unknown"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                googleMap.setOnCameraIdleListener(PostActivity.this);
                //m_map.setOnCameraMoveStartedListener(this);
                googleMap.setOnMarkerClickListener(PostActivity.this);
                googleMap.setOnCameraMoveListener(PostActivity.this);
                googleMap.setOnCameraMoveCanceledListener(PostActivity.this);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {

                Toast.makeText(getApplicationContext(), "Lat:"+mMarker.getPosition().latitude+" long:"+mMarker.getPosition().longitude,
                        Toast.LENGTH_LONG).show();
                        latlangEditTxt.setText(mMarker.getPosition().latitude +" , "+mMarker.getPosition().longitude);
                        alertDialog.dismiss();

            }

        });
        alertDialog.show();
    }

    @Override
    public void onCameraIdle() {
        try {
            Geocoder geo = new Geocoder(PostActivity.this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(mMarker.getPosition().latitude, mMarker.getPosition().longitude, 1);
            if (addresses.isEmpty()) {
                mMarker = mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).anchor(0.5f, .05f).title("Unknown"));
                mMarker.showInfoWindow();

                //yourtextfieldname.setText("Waiting for Location");
            }
            else {
                addresses.size();
                mMarker = mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).anchor(0.5f, .05f).title(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName()));
                //Toast.makeText(getApplicationContext(), "Address:- " + addresses.get(0).getFeatureName() + addresses.get(0).getAdminArea() + addresses.get(0).getLocality(), Toast.LENGTH_LONG).show();
                mMarker.showInfoWindow();
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
        }

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {
        //Remove previous center if it exists
        if (mMarker != null) {
            mMarker.remove();
            mMarker.setVisible(false);
        }
        mMap.clear();
        CameraPosition test =mMap.getCameraPosition();
        //Assign mCenterMarker reference:
        mMarker = mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).anchor(0f, 0f));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @OnClick(R.id.input_latlang)
    void showMap(){
        setLocation();
    }

    /**
     * get last known location
     */

    private void getLastKnownLocation() {
        Log.d(TAG, "Get Last Known lcoation: called");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    Location location=task.getResult();
                    //GeoPoint geoPoint=new GeoPoint(location.getLatitude(),location.getLongitude());
                    Log.d(TAG,"OnComplete: latitude: "+location.getLatitude());
                    Log.d(TAG,"OnComplete: longitude: "+location.getLongitude());
                    //latLng=new LatLng(location.getLatitude(),location.getLongitude());
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                }
            }
        });
    }

    //input field validation function
    public boolean validate() {
        boolean valid = true;

        String title = titleEditTxt.getText().toString();
        String avilableDate =dateEditTxt.getText().toString();
        String rent = rentEditTxt.getText().toString();
        String mobile = mobileEditTxt.getText().toString();
        String address = addressEditTxt.getText().toString();
        String location =latlangEditTxt.getText().toString();
        String description = descriptionEditTxt.getText().toString();

        if (title.isEmpty()) {
            titleEditTxt.setError("Enter Title");
            valid = false;
        } else {
            titleEditTxt.setError(null);
        }

        if (avilableDate.isEmpty()) {
            dateEditTxt.setError("enter date");
            valid = false;
        } else {
            dateEditTxt.setError(null);
        }
        if (rent.isEmpty() ||rent.equalsIgnoreCase("0")) {
            rentEditTxt.setError("Enter rent");
            valid = false;
        } else {
            rentEditTxt.setError(null);
        }

        if (mobile.isEmpty() || mobile.length()!=11) {
            mobileEditTxt.setError("Enter Valid Mobile Number");
            valid = false;
        } else {
            mobileEditTxt.setError(null);
        }

        if (address.isEmpty()) {
            addressEditTxt.setError("Enter Address");
            valid = false;
        } else {
            addressEditTxt.setError(null);
        }

        if (location.isEmpty() ) {
            latlangEditTxt.setError("Password Do not match");
            valid = false;
        } else {
            latlangEditTxt.setError(null);
        }
        if (description.isEmpty() ) {
            descriptionEditTxt.setError("Password Do not match");
            valid = false;
        } else {
            descriptionEditTxt.setError(null);
        }

        return valid;
    }


}

package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Adapter.CustomInfoWindow;
import com.example.rentbd.Model.InfoWindowData;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NearByActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "NearByActivity";
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleMap mMap;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    @BindView(R.id.input_search1)
    EditText searchEditTxt;

    private Marker mMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by);
        mAuth= FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ButterKnife.bind(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //getLastKnownLocation();
//        Log.d(TAG,"OnCreate: latitude: "+latitude);
//        Log.d(TAG,"OnCreate: longitude: "+longitude);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * get last known location
     */

    private void getLastKnownLocation(GoogleMap googleMap) {
        LatLng latLng;
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
                    double latitude=location.getLatitude();
                    double longitude=location.getLongitude();
                    setMap(latitude,longitude,googleMap);

                }
            }
        });
    }

    private void setMap(double latitude,double longitude,GoogleMap googleMap){
        if (ActivityCompat.checkSelfPermission(NearByActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(NearByActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        mMap=googleMap;
        LatLng posisiabsen = new LatLng(latitude, longitude); ////your lat lng
        mMarker=googleMap.addMarker(new MarkerOptions().position(posisiabsen).title("My Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        getNearByLocation(latitude,longitude);
        init();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
       getLastKnownLocation(googleMap);
    }

    private void init(){
        Log.d("Initialization:","Init");
        searchEditTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Log.d("get:","get");
                if(i== EditorInfo.IME_ACTION_SEARCH || i==EditorInfo.IME_ACTION_DONE||i==EditorInfo.IME_ACTION_NEXT
                    || keyEvent.getAction()==KeyEvent.ACTION_DOWN
                    || keyEvent.getAction()==KeyEvent.KEYCODE_ENTER){

                    //execute methode for search
                    geoLocation();

                }
                //geoLocation();
                return false;
            }
        });
        hideKeybord();
    }
    private void geoLocation(){
        Log.d("Search: ","Geolocation");

        String searchString=searchEditTxt.getText().toString();
        Locale locale = new Locale("bn", "BD");
        Geocoder geocoder=new Geocoder(NearByActivity.this, locale);
        List<Address> list=new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchString,1);
        }catch (IOException e){
            Log.e("Error: ","GeoLocation IOException "+e.getMessage());
        }
        if(list.size()>0){
            Address address=list.get(0);
            Log.d("Result:","Found location: "+address.toString());
            if (mMarker != null) {
                mMarker.remove();
                mMarker.setVisible(false);
            }
            mMap.clear();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),14,address.getAddressLine(0));
            getNearByLocation(address.getLatitude(),address.getLongitude());
        }

    }

    private void moveCamera(LatLng latLng,float zoom,String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        MarkerOptions markerOptions=new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(markerOptions);
        hideKeybord();
    }

    private void hideKeybord(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * get nearby location from firebase database using geoFire
     * @param latitude
     * @param longitude
     */
    private void getNearByLocation(double latitude,double longitude){
        ArrayList<GeoLocation> nearbyLocations=new ArrayList<>();
//        ArrayList<String> keys=new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("PostLocations");
        GeoFire geoFire = new GeoFire(ref);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 2.6);
        Log.d("Near by: ","Access"+latitude+" "+longitude);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                //Log.d("Near by: ",location.latitude+","+location.longitude);
                //Log.d("DataSnapshot: ",String.valueOf(dataSnapshot.);
//                keys.add(dataSnapshot.getKey());
                nearbyLocations.add(location);
                /*
                retrive all post data and show infowindow is not work properly.Todo
                 */
                retrivePostData(dataSnapshot.getKey());

            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {


            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                Log.d("Near by: ", String.valueOf(nearbyLocations.size()));
                for(GeoLocation location:nearbyLocations){
                    LatLng posisiabsen = new LatLng(location.latitude, location.longitude); ////your lat lng
                    mMarker=mMap.addMarker(new MarkerOptions().position(posisiabsen).title("Unknown"));

                }
//                System.out.print("Size:"+keys.size());

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    /**
     * retrive post data from firebase datbase
     */

    private void retrivePostData(String key){
        DatabaseReference mDatabaseReference= database.getReference().child("Posts").child(key);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Post post = dataSnapshot.getValue(Post.class);
//                                                Log.d("User:",user.getUsername());
                    if(post!=null){
                        setData(post);
                    }
                    else {
                        Toast.makeText(NearByActivity.this, "User value is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setData(Post post){
        InfoWindowData info = new InfoWindowData();
        info.setTitle(post.getTitle());
        info.setType(post.getType());
        info.setRent(post.getRent());
        info.setPhn(post.getMobileNumber());
        info.setAddress(post.getAddress());
        info.setAvailableDate(post.getAvailableDate());
        CustomInfoWindow customInfoWindow = new CustomInfoWindow(this);
        mMap.setInfoWindowAdapter(customInfoWindow);
        mMarker.setTag(info);
        mMarker.showInfoWindow();
    }


}

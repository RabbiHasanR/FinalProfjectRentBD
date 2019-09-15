package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.rentbd.Adapter.CustomInfoWindow;
import com.example.rentbd.Model.InfoWindowData;
import com.example.rentbd.Model.Post;
import com.example.rentbd.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShowInMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
//    private String key;
    double[] latitude = new double[1];
    double[] longitude = new double[1];
    private GoogleMap map;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_in_map);
        mAuth= FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        Intent intent=getIntent();
        if(intent.hasExtra("key")){
           String key=intent.getExtras().getString("key");
            Toast.makeText(this, intent.getExtras().toString(), Toast.LENGTH_SHORT).show();
            retriveLocation(key);
        }


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
    }

    private void retriveLocation(String key){
        DatabaseReference ref= database.getReference().child("PostLocations");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    System.out.println(String.format("The location for key %s is [%f,%f]", key, location.latitude, location.longitude));
                    latitude[0] =location.latitude;
                    longitude[0] =location.longitude;
                    System.out.println("Location 1:"+latitude[0]+" , "+longitude[0]);
                    setMap(latitude[0],longitude[0],key);
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);

            }
        });
    }

    private void setMap(double latitude,double longitude,String key){
        if (ActivityCompat.checkSelfPermission(ShowInMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(ShowInMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        LatLng posisiabsen = new LatLng(latitude, longitude); ////your lat lng
        mMarker=map.addMarker(new MarkerOptions().position(posisiabsen).title("Post Location"));
        retrivePostData(key);
        map.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
        map.getUiSettings().setZoomControlsEnabled(true);
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
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
                        Toast.makeText(ShowInMapActivity.this, "User value is null", Toast.LENGTH_SHORT).show();
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
        map.setInfoWindowAdapter(customInfoWindow);
        mMarker.setTag(info);
        mMarker.showInfoWindow();
    }
}

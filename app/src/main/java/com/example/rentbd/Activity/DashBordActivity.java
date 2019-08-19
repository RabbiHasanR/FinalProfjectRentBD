package com.example.rentbd.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.rentbd.Model.User;
import com.example.rentbd.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashBordActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "DashBordActivity";
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private String userId;
    private boolean isRetriveImage=false;
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseReference;
    private TextView userNameTxt;
    private ImageView profileImageView;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    //location permission
    public static final int ERROR_DIALOG_REQUEST = 9001;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS=9002;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION=9003;
    private boolean mLocationPermissionGranted = false;

    @BindView(R.id.mainGrid)
    GridLayout gridLayout;

    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setSupportActionBar(toolbar);
        mAuth= FirebaseAuth.getInstance();
        mStorage= FirebaseStorage.getInstance().getReference().child("Photos");
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        mDatabaseReference= database.getReference().child("Users");
        if(mAuth!=null){
            userId=mAuth.getCurrentUser().getUid();
        }
        ButterKnife.bind(this);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(DashBordActivity.this,PostActivity.class);
                startActivity(intent);
            }
        });
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        retriveImageFromFireStore();
        if(!isRetriveImage){
            Glide.with(DashBordActivity.this)
                    .load(R.drawable.noimageicon)
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImageView);
        }
        readDataFromDatabase();
        setSingleEvent(gridLayout);
    }

    private void findView(){
        navigationView=(NavigationView)findViewById(R.id.nav_view);
        View headerLayout = navigationView.getHeaderView(0);
        profileImageView=(ImageView)headerLayout.findViewById(R.id.profileImageView);
        userNameTxt=(TextView)headerLayout.findViewById(R.id.user_name);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        drawer=(DrawerLayout)findViewById(R.id.drawer_layout);
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
                Glide.with(DashBordActivity.this)
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                isRetriveImage=false;
                Toast.makeText(DashBordActivity.this, "Failed retrive Image.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * read data from firebase database by user id
     */
    private void readDataFromDatabase(){
        DatabaseReference uidRef=mDatabaseReference.child(userId);
        // Attach a listener to read the data for specific user
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User user = dataSnapshot.getValue(User.class);
//                                                Log.d("User:",user.getUsername());
                    if(user!=null){
                        userNameTxt.setText(user.getUsername());
                    }
                    else {
                        Toast.makeText(DashBordActivity.this, "User value is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
        else if(id==R.id.action_nearby){
            Intent intent=new Intent(this,NearByActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            moveProfileActivity();
        } else if (id == R.id.nav_new_post) {
            movePostActivity();

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_report_problem) {

        }
        else if (id==R.id.nav_logout){
            if(mAuth.getCurrentUser()!=null){
                mAuth.signOut();
                moveLoginActivity();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void moveProfileActivity(){
        Intent intent=new Intent(this,ProfileActivity.class);
        startActivity(intent);
    }

    private void moveToLetActivity(String type){
        Intent intent=new Intent(this,ToLetActivity.class);
        intent.putExtra("type",type);
        startActivity(intent);
    }

    // we are setting onClickListener for each element
    private void setSingleEvent(GridLayout gridLayout) {
        for(int i = 0; i<gridLayout.getChildCount();i++){
            CardView cardView=(CardView)gridLayout.getChildAt(i);
            final int finalI= i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(finalI==0){
                        Toast.makeText(DashBordActivity.this, "Clicked at Mess", Toast.LENGTH_SHORT).show();
                        type="mess";
                        moveToLetActivity(type);
                    }
                    else if(finalI==1){
                        Toast.makeText(DashBordActivity.this, "Clicked at Hostel", Toast.LENGTH_SHORT).show();
                        type="hostel";
                        moveToLetActivity(type);
                    }
                    else if(finalI==2){
                        Toast.makeText(DashBordActivity.this, "Clicked at Flat", Toast.LENGTH_SHORT).show();
                        type="flat";
                        moveToLetActivity(type);
                    }
                    else if(finalI==3){
                        Toast.makeText(DashBordActivity.this, "Clicked at Sublet", Toast.LENGTH_SHORT).show();
                        type="sublet";
                        moveToLetActivity(type);
                    }
                    else if(finalI==4){
                        Toast.makeText(DashBordActivity.this, "Clicked at Garage", Toast.LENGTH_SHORT).show();
                        type="garage";
                        moveToLetActivity(type);
                    }
                    else if(finalI==5){
                        Toast.makeText(DashBordActivity.this, "Clicked at Office", Toast.LENGTH_SHORT).show();
                        type="office";
                        moveToLetActivity(type);
                    }
                }
            });
        }
    }

    /**
     * move to post activity
     */
    private void movePostActivity(){
        Intent intent=new Intent(this,PostActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.fab)
    void onClickFab(){
        movePostActivity();
    }
    private void moveLoginActivity(){
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    /**
     * check location permission
     */

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            Toast.makeText(this, "Location Permissiong granted", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(DashBordActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(DashBordActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
//                    getChatrooms();
//                    getUserDetails();
                    //getLastKnownLocation();
                    Toast.makeText(this, "Location Permission granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkMapServices()){
            if(mLocationPermissionGranted){
                //getLastKnownLocation();
                Toast.makeText(this, "Location Permission granted", Toast.LENGTH_SHORT).show();
            }
            else{
                getLocationPermission();
                //mMapView.onResume();
            }
        }
    }


}

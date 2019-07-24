package com.example.rentbd.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Model.User;
import com.example.rentbd.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase database;
    private User user;
    private String userId;

    @BindView(R.id.user_name_txt)
    TextView user_name_txt;
    @BindView(R.id.user_email_txt)
    TextView user_email_txt;
    @BindView(R.id.user_phone_txt)
    TextView user_phone_txt;
    @BindView(R.id.user_address_txt)
    TextView user_address_txt;
    @BindView(R.id.user_gender_txt)
    TextView user_gender_txt;
    @BindView(R.id.user_edit_btn)
    Button user_edit_btn;
    @BindView(R.id.info_layout)
    ScrollView info_layout;
    @BindView(R.id.edit_layout)
    ScrollView edit_layout;
    @BindView(R.id.edit_name)
    EditText nameEditText;
    @BindView(R.id.edit_mobile)
    EditText mobileEditText;
    @BindView(R.id.edit_address)
    EditText addressEditText;
    @BindView(R.id.edit_gender)
    EditText genderEditText;
    @BindView(R.id.btn_save)
    Button updateInfoBtn;

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_info, container, false);

        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        mDatabaseReference= database.getReference().child("Users");
        user=new User();
        ButterKnife.bind(this,view);
        if(mAuth!=null){
            userId=mAuth.getCurrentUser().getUid();
        }
        readDataFromDatabase();
        // Inflate the layout for this fragment
        return view;
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
                    user = dataSnapshot.getValue(User.class);
//                                                Log.d("User:",user.getUsername());
                    if(user!=null){
                        setDataOnTxtView(user);
                        setDataOnEditText(user);
                    }
                    else {
                        Toast.makeText(getContext(), "User value is null", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private void setDataOnTxtView(User user){
        user_name_txt.setText(user.getUsername());
        user_email_txt.setText(user.getEmail());
        user_phone_txt.setText(user.getPhone());
        user_address_txt.setText(user.getAddress());
        user_gender_txt.setText(user.getGender());
    }

    private void setDataOnEditText(User user){
        nameEditText.setText(user.getUsername());
        mobileEditText.setText(user.getPhone());
        addressEditText.setText(user.getAddress());
        genderEditText.setText(user.getGender());
    }

    @OnClick(R.id.user_edit_btn)
    void editInfo(){
        String text=user_edit_btn.getText().toString().trim();
        if(text.equalsIgnoreCase("edit")){
            info_layout.setVisibility(View.INVISIBLE);
            edit_layout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_save)
    void svaeEditInfo(){
        String text=updateInfoBtn.getText().toString().trim();
        if(text.equalsIgnoreCase("update")){
            updateUserInfo();
            edit_layout.setVisibility(View.INVISIBLE);
            info_layout.setVisibility(View.VISIBLE);
            readDataFromDatabase();
        }

    }

    private void updateUserInfo(){
        final ProgressDialog progressDialog = new ProgressDialog(getContext(),
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();
        String name=nameEditText.getText().toString().trim();
        String mobile=mobileEditText.getText().toString().trim();
        String address=addressEditText.getText().toString().trim();
        String gender=genderEditText.getText().toString().trim();
        user.setUsername(name);
        user.setPhone(mobile);
        user.setAddress(address);
        user.setGender(gender);
        mDatabaseReference.child(userId).setValue(user);
        progressDialog.dismiss();
        Toast.makeText(getContext(), "Update data", Toast.LENGTH_SHORT).show();
    }

}

package com.example.rentbd.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rentbd.Model.User;
import com.example.rentbd.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase database;
    private User user;
    private ProgressDialog progressDialog;
    private Intent intent;

    @BindView(R.id.input_email)
    EditText _emailText;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        //get firebase database instance and reference
        mDatabaseReference= database.getReference().child("Users");
        user=new User();
        intent=new Intent();
        intent=LoginActivity.this.getIntent();
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                move_signup_activity();
            }
        });
    }

    private void storeUserInfo(String userId){
        Log.d("Store user Info:","access");
        if(intent.hasExtra("name")){
            Log.d("Extras:",intent.getExtras().getString("name")+"Store");
            Log.d("Intent:","Not Null");
            String name=intent.getExtras().getString("name");
            String email=intent.getExtras().getString("email");
            String password=intent.getExtras().getString("password");
            String phone=intent.getExtras().getString("phone");
            String address=intent.getExtras().getString("address");
            String gender=intent.getExtras().getString("gender");
            user.setUsername(name);
            user.setAddress(address);
            user.setEmail(email);
            user.setGender(gender);
            user.setPassword(password);
            user.setPhone(phone);
            mDatabaseReference.child(userId).setValue(user);
            Toast.makeText(this, "Save data", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.dismiss();
            Log.d("Intent:","Null");
        }
    }

    /**
     * move one activity to another with intent
     */
    private void move_signup_activity(){
        // Start the Signup activity
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void move_dashbord_activity(){
        // Start the Profile activity
        Intent intent = new Intent(getApplicationContext(), DashBordActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    /**
     * user login
     */
    private void login() {
        Log.d(TAG, "Login");
        //check validation
        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString().trim();
        String password = _passwordText.getText().toString().trim();

        // Implement firebase authentication login here.
        auth_sign_in(email,password);
    }


    private void hasEmailInDatabase(String userId){
        DatabaseReference mDatabaseReference= database.getReference().child("Users").child(userId);
        Log.d("Method:","access");
        Log.d("User Id:",userId);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d("User:","old");
                    onLoginSuccess();
                    move_dashbord_activity();
                    progressDialog.dismiss();
//                    user = dataSnapshot.getValue(User.class);
//                    if(user.getEmail().equals(email)){
//
//                    }

                }
                else {
                    if(intent.hasExtra("name")){
                        storeUserInfo(userId);
                        onLoginSuccess();
                        move_dashbord_activity();
                        progressDialog.dismiss();
                    }
                    else {
                        progressDialog.dismiss();
                        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                onLoginFailed();
                            }
                        });
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                _loginButton.setVisibility(View.VISIBLE);
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
        // Attach a listener to read the data for specific user
       /* ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addListenerForSingleValueEvent(valueEventListener);*/
    }
    /**
     *Firebase authentiation sign in
     */
    private void auth_sign_in(String email,String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    if(checkIfEmailVerified()){
                        String userId=mAuth.getCurrentUser().getUid();
                        hasEmailInDatabase(userId);
                    }
                    else {
                        if(intent.hasExtra("name")){
                            Toast.makeText(LoginActivity.this, intent.getExtras().getString("name")+"auth", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Email not varified", Toast.LENGTH_SHORT).show();
                            _loginButton.setEnabled(true);

                        }
                        else {
                            progressDialog.dismiss();
                            firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    onLoginFailed();
                                }
                            });
                        }

                    }
                }
                else {
                    progressDialog.dismiss();
                    onLoginFailed();
                }
            }
        });

    }

    /**
     * check if email veriied or not
     * @return
     */
    private boolean checkIfEmailVerified()
    {
        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser.isEmailVerified())
        {
            // user is verified return true.
            return true;
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.
            return false;
        }
    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//
//                // TODO: Implement successful signup logic here
//                // By default we just finish the Activity and log them in automatically
//                this.finish();
//            }
//        }
//
//    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    /**
     * show login success message
     */
    private void onLoginSuccess() {
        Toast.makeText(getBaseContext(), "Login success", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
        finish();
    }
    /**
     * show login failed message
     */
    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Incorrect Email or Password", Toast.LENGTH_LONG).show();
        _loginButton.setEnabled(true);
    }

    /**
     * validate every input field
     * @return
     */
    private boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null){
            mAuth.signOut();
        }
    }

    @OnClick(R.id.link_forgot_password)
    void forgotPassord(){
        move_reset_password_activity();
    }

    private void move_reset_password_activity(){
        // Start the Profile activity
        Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

}

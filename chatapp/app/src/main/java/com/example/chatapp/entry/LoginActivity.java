package com.example.chatapp.entry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    //private FirebaseUser currentUser;
    private EditText loginEmailEditText, loginPasswordEditText;
    private Button loginButton, phoneButton;
    private TextView forgetPasswordTextView, needNewAccountTextView, loginUsingYourPhoneTextView;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;
    private DatabaseReference usersRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitFields();

        needNewAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentUserLogin();
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

//        forgetPasswordTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                forgetPassword();
//            }
//        });


    }

    private void currentUserLogin() {
        String email = loginEmailEditText.getText().toString();
        String password = loginPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingDialog.setTitle("Logging in");
            loadingDialog.setMessage("Please wait");
            loadingDialog.setCanceledOnTouchOutside(true);
            loadingDialog.show();

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                //twra tha prepei na swsoume to token mesa sto Users
                                usersRef.child(currentUserID).child("device_token")
                                        .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            sendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Logged in successfully",Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    }
                                });

                            }
                            else {

                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                            }
                        }
                    });

        }
    }

    private void forgetPassword(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = loginEmailEditText.getText().toString();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void InitFields() {
        loginEmailEditText = findViewById(R.id.edittext_login_email);
        loginPasswordEditText = findViewById(R.id.edittext_login_password);
        loginButton = findViewById(R.id.button_login);
        phoneButton = findViewById(R.id.button_phone_login);
        forgetPasswordTextView = findViewById(R.id.textview_forget_password);
        needNewAccountTextView = findViewById(R.id.textview_new_account);
        loginUsingYourPhoneTextView = findViewById(R.id.textview_login_using);
        loadingDialog = new ProgressDialog(this);




    }


    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity(){
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}

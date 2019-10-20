package com.example.chatapp.entry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmailEditText, registerPasswordEditText;
    private Button registerButton;
    private TextView alreadyHaveAccountTextView;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initFields();
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        alreadyHaveAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginActivity();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String email = registerEmailEditText.getText().toString();
        String password = registerPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter your email!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else {

            loadingDialog.setTitle("Creating new account");
            loadingDialog.setMessage("Please wait");
            loadingDialog.setCanceledOnTouchOutside(true);
            loadingDialog.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){

                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(currentUserID).setValue("");

                                rootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account created Successfully", Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                            }
                        }
                    });
        }

    }


    private void initFields() {
        registerButton = findViewById(R.id.register_button);
        registerEmailEditText = findViewById(R.id.edittext_register_email);
        registerPasswordEditText = findViewById(R.id.edittext_register_password);
        alreadyHaveAccountTextView = findViewById(R.id.textview_already_have_account);
        loadingDialog = new ProgressDialog(this);
    }

    private void sendUserToMainActivity(){
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity(){
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

}

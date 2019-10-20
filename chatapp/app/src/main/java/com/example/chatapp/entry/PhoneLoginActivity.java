package com.example.chatapp.entry;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText phoneInputEdittext, verifyCodeInputEdittext;
    private Button verifyCodeToBeSentButton, verifyButton;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        initFields();

        loadingDialog = new ProgressDialog(this);

        verifyCodeToBeSentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phoneNumber = phoneInputEdittext.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this,"Please enter your phone number",Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingDialog.setTitle("Phone Verification");
                    loadingDialog.setMessage("please wait for authentication");
                    loadingDialog.setCanceledOnTouchOutside(false);
                    loadingDialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,                      // Phone number to verify
                            60,                            // Timeout duration
                            TimeUnit.SECONDS,                // Unit of timeout
                            PhoneLoginActivity.this, // Activity (for callback binding)
                            mCallbacks);                    // OnVerificationStateChangedCallbacks
                }

            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCodeToBeSentButton.setVisibility(View.INVISIBLE);
                phoneInputEdittext.setVisibility(View.INVISIBLE);

                String verificationCode = verifyCodeInputEdittext.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter code", Toast.LENGTH_SHORT).show();
                }
                else{

                    loadingDialog.setTitle("Code Verification");
                    loadingDialog.setMessage("please wait for verification");
                    loadingDialog.setCanceledOnTouchOutside(false);
                    loadingDialog.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this,"Invalid phone number, try again!", Toast.LENGTH_SHORT).show();

                verifyCodeToBeSentButton.setVisibility(View.VISIBLE);
                phoneInputEdittext.setVisibility(View.VISIBLE);
                verifyCodeInputEdittext.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                //Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();

                verifyCodeToBeSentButton.setVisibility(View.INVISIBLE);
                phoneInputEdittext.setVisibility(View.INVISIBLE);
                verifyCodeInputEdittext.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);



            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = task.getResult().getUser();

                            loadingDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "You have logged in successfully", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }

                        else
                            {
                            // Sign in failed, display a message and update the UI
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid

                                String messageError = task.getException().toString();
                                Toast.makeText(PhoneLoginActivity.this,"Error:" + messageError, Toast.LENGTH_SHORT).show();
                            }
                        }
                });
    }

    private void initFields() {
        phoneInputEdittext = findViewById(R.id.phone_input_edittext);
        verifyCodeInputEdittext = findViewById(R.id.verification_number_edittext);
        verifyCodeToBeSentButton = findViewById(R.id.verification_number_send_button);
        verifyButton = findViewById(R.id.verify_button);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

}

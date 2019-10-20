package com.example.chatapp.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    private Button updateSettingsButton;
    private EditText username, userstatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private static final int GALLERY_CODE = 1;
    private StorageReference userProfileImagesRef;
    private ProgressDialog loadingDialog;
    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference();
        userProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        InitFields();

        username.setVisibility(View.INVISIBLE);

        updateSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSettings();
            }
        });

        retrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });

    }

    private void retrieveUserInfo() {
         reference.child("Users").child(currentUserID)
                 .addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))
                         && (dataSnapshot.hasChild("image")))
                         {
                             String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                             String retrieveUserstatus = dataSnapshot.child("status").getValue().toString();
                             String retrieveProfImage = dataSnapshot.child("image").getValue().toString();

                             username.setText(retrieveUsername);
                             userstatus.setText(retrieveUserstatus);
                             Picasso.get().load(retrieveProfImage).into(userProfileImage);


                         }
                         else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                         {
                             String retrieveUsername = Objects.requireNonNull(dataSnapshot.child("name").getValue().toString());
                             String retrieveUserstatus = Objects.requireNonNull(dataSnapshot.child("status").getValue().toString());

                             //String retrieveProfImage = dataSnapshot.child("image").getValue().toString();

                             username.setText(retrieveUsername);
                             userstatus.setText(retrieveUserstatus);
                         }
                         else
                         {
                             username.setVisibility(View.VISIBLE);
                             Toast.makeText(SettingsActivity.this, "Please set & update your profil information", Toast.LENGTH_SHORT).show();
                         }

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
    }

    private void updateSettings() {
        String setUsername = username.getText().toString();
        String setStatus = userstatus.getText().toString();
        if (TextUtils.isEmpty(setUsername))
        {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this,"Please enter your status",Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
               profileMap.put("uid", currentUserID);
               profileMap.put("name", setUsername);
               profileMap.put("status", setStatus);
            reference.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this,"Profile Updated",Toast.LENGTH_SHORT).show();
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void InitFields() {
        updateSettingsButton = findViewById(R.id.update_settings_button);
        username = findViewById(R.id.edittext_set_username);
        userstatus = findViewById(R.id.edittext_set_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingDialog = new ProgressDialog(this);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Settings");

    }

    //Override onActivityResult method in your activity to get crop result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data!=null){
            Uri imageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){

                loadingDialog.setTitle("Set Profile Image");
                loadingDialog.setMessage("Your profile image is updating");
                loadingDialog.setCanceledOnTouchOutside(false);
                loadingDialog.show();

                final Uri resultUri = result.getUri(); //this uri contains the crop image

                //store crop image inside firebase
                final StorageReference filePath = userProfileImagesRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUri = uri.toString();

                                reference.child("Users").child(currentUserID).child("image")
                                        .setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(SettingsActivity.this, "Profile Image saved to database", Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                        else{
                                            String errorMessage = task.getException().toString();
                                            Toast.makeText(SettingsActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }


        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}

package com.example.chatapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.example.chatapp.activity.FindFriendsActivity;
import com.example.chatapp.activity.SettingsActivity;
import com.example.chatapp.adapter.TabsAccessorAdapter;
import com.example.chatapp.entry.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mytabsAccessorAdapter;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        rootRef = FirebaseDatabase.getInstance().getReference();

        initFields();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(currentUser == null){
            sendUserToLoginActivity();
        }
        else {
            updateUserDetails("online");
            verifyUserExist();
        }
    }

    //this method is called everytime the app is minimized etc
    @Override
    protected void onStop() {
        super.onStop();

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            updateUserDetails("offline");
        }
    }

    //if app crashed
    @Override
    protected void onDestroy() {
        super.onDestroy();

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            updateUserDetails("offline");
        }
    }

    private void verifyUserExist() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if (item.getItemId() == R.id.logout_options) {
             updateUserDetails("offline");
             mAuth.signOut();
             sendUserToLoginActivity();
         }
         if (item.getItemId() == R.id.find_friends_options){
             sendUserToFindFriendsActivity();
         }
         if(item.getItemId() == R.id.settings_options){
             sendUserToSettingsActivity();
         }
         if (item.getItemId() == R.id.create_group_options){
             requestForNewGroup();
         }
         return true;
    }

    private void initFields(){
        mainToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("ChitChat");
        myViewPager =findViewById(R.id.main_tab_pager);
        mytabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mytabsAccessorAdapter);
        myTabLayout = findViewById(R.id.main_tab);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity(){
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        //settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        //finish();
    }

    private void sendUserToFindFriendsActivity(){
        Intent findFriendsIntent = new Intent(this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void requestForNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name: ");
        final EditText GROUP_NAME_FIELD = new EditText(MainActivity.this);
        GROUP_NAME_FIELD.setHint("e.g Group Study");
        builder.setView(GROUP_NAME_FIELD);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = GROUP_NAME_FIELD.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(MainActivity.this,"Please insert a group name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createGroupChat(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void createGroupChat(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, groupName + " group has created", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserDetails(String status){
        String currentTime, currentDate;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = simpleDateFormat.format(calendar.getTime());
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = simpleTimeFormat.format(calendar.getTime());

        HashMap<String, Object> userDetailsMap = new HashMap<>();
        userDetailsMap.put("date",currentDate);
        userDetailsMap.put("time", currentTime);
        userDetailsMap.put("state", status);

        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).child("user_status")
                .updateChildren(userDetailsMap);
    }
}

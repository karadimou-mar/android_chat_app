package com.example.chatapp.activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import android.support.v7.widget.Toolbar;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    private Toolbar myToolbar;
    private ScrollView myScrollView;
    private ImageButton sendMessageButton;
    private EditText messageInput;
    private TextView displayMessagesTextView;

    private String currentGroupName, currentUserID, currentUserName, currentDate, currentTime;
    private FirebaseAuth currentUserAuth;
    private DatabaseReference userReference, groupNameReference, groupMessageKeyReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("group name").toString();

        currentUserAuth = FirebaseAuth.getInstance();
        currentUserID = currentUserAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        //Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        initFields();
        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveInputMessageToDatabase();

                messageInput.setText("");
                myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    displayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void initFields() {
        sendMessageButton = findViewById(R.id.groupchat_send_message_button);
        messageInput = findViewById(R.id.input_groupchat_edittext);
        displayMessagesTextView = findViewById(R.id.groupchat_textview);

        myToolbar = findViewById(R.id.groupchat_bar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        myScrollView = findViewById(R.id.groupchat_scroll_view);
    }

    private void getUserInfo() {
        userReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveInputMessageToDatabase() {
        String inputTextMessage = messageInput.getText().toString();
        String messageKey  = groupNameReference.push().getKey(); //????????????

        if (TextUtils.isEmpty(inputTextMessage)){
            Toast.makeText(this,"Please insert a message", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat =  new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameReference.updateChildren(groupMessageKey);

            groupMessageKeyReference = groupNameReference.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                 messageInfoMap.put("name", currentUserName);
                 messageInfoMap.put("message",inputTextMessage);
                 messageInfoMap.put("date",currentDate);
                 messageInfoMap.put("time", currentTime);

            groupMessageKeyReference.updateChildren(messageInfoMap);

        }
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayMessagesTextView.append(chatName + " :\n" + chatMessage + " \n" + chatTime + "        " + chatDate +"\n\n");
            myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}

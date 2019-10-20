package com.example.chatapp.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, currentState;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, cancelChatRequestButton;
    private DatabaseReference userRef, chatRequestRef, contactRef, notificationRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("online_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        initFields();
        currentState = "new";
        retrieveUserInfo();
    }

    private void initFields() {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName =  findViewById(R.id.visit_username);
        userProfileStatus = findViewById(R.id.visit_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        cancelChatRequestButton = findViewById(R.id.decline_message_request_button);
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists())  && (dataSnapshot.hasChild("image"))) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    //to placehololder mpainei se periptwsi pou o user den exei valei image (to profile image einai optional)
                    Picasso.get().load(userImage).placeholder(R.drawable.ic_person_black).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }
                //if user does not have an image picture
                else{
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void manageChatRequest(){

        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent"))
                            {
                                currentState = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            }
                            else if (request_type.equals("received"))
                            {
                                currentState = "request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");
                                cancelChatRequestButton.setVisibility(View.VISIBLE);
                                cancelChatRequestButton.setEnabled(true);

                                cancelChatRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                currentState = "friends";
                                                sendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!senderUserID.equals(receiverUserID))
        {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sendMessageRequestButton.setEnabled(false);

                    if (currentState.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if (currentState.equals("request_sent"))
                    {
                        cancelChatRequest();
                    }
                    if (currentState.equals("request_received"))
                    {
                        acceptChatRequest();
                    }
                    if (currentState.equals("friends"))
                    {
                        removeContact();
                    }
                }
            });
        }
        else
        {
           sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserID);
                                                chatNotificationMap.put("type", "request");

                                                notificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    currentState = "request_sent";
                                                                    sendMessageRequestButton.setText("Cancel Chat Request");
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest(){
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                //cancelChatRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void acceptChatRequest(){
        contactRef.child(senderUserID).child(receiverUserID)
                .child("Contacts List").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts List").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    sendMessageRequestButton.setEnabled(true);
                                                                                    currentState = "friends";
                                                                                    sendMessageRequestButton.setText("Remove this Contact");
                                                                                    cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                                                    //cancelChatRequestButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void removeContact() {
        contactRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful())
                                            {
                                                sendMessageRequestButton.setEnabled(true);
                                                 currentState = "new";
                                                sendMessageRequestButton.setText("Send Message");

                                                cancelChatRequestButton.setVisibility(View.INVISIBLE);
                                                //cancelChatRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

}

/**
 * no need to retrieve data again,
 * we will take it from chatFragment -> RETRIEVED_NAME, USERS_ID, RETRIEVED_IMAGE
 */


package com.example.chatapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.model.Messages;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import android.support.v7.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.HashMap;


public class ChatActivity extends AppCompatActivity {

    //edw irtha mesw tou intent apo to chatFragment
    //prepei na parw ta data (userid, username) apo ton  xristi-filo  pou exw sinomilisei
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView username, lastSeen;
    private CircleImageView userImage;

    private Toolbar chatToolbar;
    private ImageButton sendMessageButton, sendFileButton, takePhotoButton;
    private EditText inputMessageText;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String currentTime, currentDate, checker = "camera", myUrl = "";
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loadingDialog;

    private static final int CODE = 44;
//    private static final int PDF_CODE = 45;
//    private static final int DOCX_CODE = 46;
    private static final int CAMERA_REQUEST_CODE = 11456;
//    private StorageReference mStorage;
//    private ProgressDialog mProgress;
//
//    private ImageView imageView = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_userID").toString();
        messageReceiverName = getIntent().getExtras().get("visit_username").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        initField();

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messageList.add(messages);
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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

        username.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.ic_person_black_24dp).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
            }
        });

        displayLastSeenDetails();

        sendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF",
                                "Doc"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select file");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) {
                            checker = "image";
                            sendImageIntent();

                        }
                        if (i == 1) {
                            checker = "pdf";
                            sendPDFIntent();
                        }
                        if (i == 2) {
                            checker = "docx";
                            sendFileIntent();
                        }
                    }
                });
                builder.show();
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePictureIntent();
            }
        });



    }

    private void sendFileIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/msword");
        startActivityForResult(Intent.createChooser(intent, "Select doc"), CODE);
    }

    private void sendPDFIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(Intent.createChooser(intent, "Select pdf"), CODE);
    }

    private void sendImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CODE);
    }

    private void takePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Choose an app"), CAMERA_REQUEST_CODE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendMessage() {

        String messageText = inputMessageText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please write a message first!", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            HashMap<String, Object> messageTextBodyMap = new HashMap<>();
            messageTextBodyMap.put("message", messageText);
            messageTextBodyMap.put("type", "text");
            messageTextBodyMap.put("from", messageSenderID);
            messageTextBodyMap.put("to", messageReceiverID);
            messageTextBodyMap.put("messageID", messagePushID);
            messageTextBodyMap.put("time", currentTime);
            messageTextBodyMap.put("date", currentDate);


            HashMap<String, Object> messageBodyDetails = new HashMap<>();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBodyMap);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBodyMap);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Toast.makeText(ChatActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                    }

                    inputMessageText.setText("");
                }
            });
        }
    }

    private void initField() {

        //add custom bar to chatActivity
        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image_circle);
        username = findViewById(R.id.custom_profile_name_textview);
        lastSeen = findViewById(R.id.custom_user_last_seen_textview);
        sendMessageButton = findViewById(R.id.send_chat_button);
        inputMessageText = findViewById(R.id.input_message_edittext);
        sendFileButton = findViewById(R.id.send_file_button);
        takePhotoButton = findViewById(R.id.take_photo_button);
        //imageView = this.findViewById(R.id.message_sender_imageview);

        messageAdapter = new MessageAdapter(messageList);
        userMessagesList = findViewById(R.id.messages_list_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        loadingDialog = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        currentDate = simpleDateFormat.format(calendar.getTime());
        SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = simpleTimeFormat.format(calendar.getTime());


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK ) {
//            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(bitmap);

  //      }

        ///////////////////////////////////////////////////////////////////////////////////

        if  (requestCode == CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingDialog.setTitle("Sending File");
            loadingDialog.setMessage("Please wait");
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.show();

            fileUri = data.getData();
            
//            if (checker.equals("camera")){
//                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
//                        .child("jgjgg");
//            }

           if (!checker.equals("image")) {

                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push();

                //auto gia na min kanei replace kapoio proigoumeno me to ido onoma px
                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(fileUri);


                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();


                            HashMap<String, Object> messageImageBodyMap = new HashMap<>();
                            messageImageBodyMap.put("message", myUrl);
                            messageImageBodyMap.put("name", fileUri.getLastPathSegment());
                            messageImageBodyMap.put("type", checker);
                            messageImageBodyMap.put("from", messageSenderID);
                            messageImageBodyMap.put("to", messageReceiverID);
                            messageImageBodyMap.put("messageID", messagePushID);
                            messageImageBodyMap.put("time", currentTime);
                            messageImageBodyMap.put("date", currentDate);


                            HashMap<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBodyMap);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBodyMap);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        loadingDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }

                                    inputMessageText.setText("");
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
//                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                        double p = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
//                        loadingDialog.setMessage((int) p + " % Uploading......");
//                    }
//                });


            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                        .child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();


                            HashMap<String, Object> messageImageBodyMap = new HashMap<>();
                            messageImageBodyMap.put("message", myUrl);
                            messageImageBodyMap.put("name", fileUri.getLastPathSegment());
                            messageImageBodyMap.put("type", checker);
                            messageImageBodyMap.put("from", messageSenderID);
                            messageImageBodyMap.put("to", messageReceiverID);
                            messageImageBodyMap.put("messageID", messagePushID);
                            messageImageBodyMap.put("time", currentTime);
                            messageImageBodyMap.put("date", currentDate);


                            HashMap<String, Object> messageBodyDetails = new HashMap<>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageImageBodyMap);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageImageBodyMap);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        loadingDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        loadingDialog.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error!", Toast.LENGTH_SHORT).show();
                                    }

                                    inputMessageText.setText("");
                                }
                            });


                        }
                    }
                });


            } else {
                loadingDialog.dismiss();
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void displayLastSeenDetails() {
        rootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("user_status").hasChild("state")) {

                    String state = dataSnapshot.child("user_status").child("state").getValue().toString();
                    String date = dataSnapshot.child("user_status").child("date").getValue().toString();
                    String time = dataSnapshot.child("user_status").child("time").getValue().toString();

                    if (state.equals("online")) {
                        lastSeen.setText("online");

                    } else if (state.equals("offline")) {
                        lastSeen.setText("Last Seen: " + date + " " + time);
                    }
                } else {
                    lastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

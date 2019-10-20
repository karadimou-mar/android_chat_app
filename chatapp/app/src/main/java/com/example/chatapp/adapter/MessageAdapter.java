package com.example.chatapp.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.chatapp.MainActivity;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.activity.ImageViewerActivity;
import com.example.chatapp.model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;
import com.example.chatapp.R;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {

        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView senderImageView, receiverImageView;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            senderImageView = itemView.findViewById(R.id.message_sender_imageview);
            receiverImageView = itemView.findViewById(R.id.message_receiver_imageview);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        final String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.ic_person_black_24dp).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.senderImageView.setVisibility(View.GONE);
        messageViewHolder.receiverImageView.setVisibility(View.GONE);

        if (fromMessageType.equals("text"))
        {

            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
            else{

                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " + messages.getDate());
            }
        }
        else if (fromMessageType.equals("image")){
            //if the online user is the sender!
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.senderImageView);

            }
            else{
                messageViewHolder.receiverImageView.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.receiverImageView);

            }
        }else if (fromMessageType.equals("pdf") || (fromMessageType.equals("docx"))) {
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.senderImageView.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp2-101a1.appspot.com/o/Image%20Files%2Fsend_file.png?alt=media&token=b1858df3-862c-4cd3-9637-4feba744c42f")
                        .into(messageViewHolder.senderImageView);

            }
            else{
                messageViewHolder.receiverImageView.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp2-101a1.appspot.com/o/Image%20Files%2Fsend_file.png?alt=media&token=b1858df3-862c-4cd3-9637-4feba744c42f")
                        .into(messageViewHolder.receiverImageView);

            }
        }

        if (fromUserID.equals(messageSenderID )){
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (userMessagesList.get(position).getType().equals("pdf") ||
                    userMessagesList.get(position).getType().equals("docx")){

                        CharSequence options[] = new CharSequence[]
                                {
                                  "Delete",
                                  "Download",
                                   "Cancel",
                                   "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessage(position,messageViewHolder);
                                }
                                else if (i == 1){

                                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(userMessagesList.get(position).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3){
                                    deleteMessageForEveryone(position, messageViewHolder);
                                }
                            }
                        });
                        builder.show();


                    }
                    else if(userMessagesList.get(position).getType().equals("text")){

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessage(position,messageViewHolder);
                                }
                                else if (i == 2){
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();


                    }
                    else if(userMessagesList.get(position).getType().equals("image")){

                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete",
                                        "Open Image",
                                        "Cancel",
                                        "Delete for Everyone"
                                };

                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete Image?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i == 0){
                                    deleteSentMessage(position,messageViewHolder);
                                    //userMessagesList.get(position).getMessage().notifyAll();
                                    //notifyItemRemoved(position);

//                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), );
//                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 1){
                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url",userMessagesList.get(position).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                }
                                else if (i == 3){
                                    deleteMessageForEveryone(position,messageViewHolder);
                                }
                            }
                        });
                        builder.show();


                    }
                }
            });
        }
        else  {
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userMessagesList.get(position).getType().equals("pdf") ||
                        userMessagesList.get(position).getType().equals("docx")){

                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete",
                                    "Download",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                deleteReceivedMessage(position, messageViewHolder);
                            }
                            else if (i == 1){

                                Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(userMessagesList.get(position).getMessage()));
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();


                }
                else if(userMessagesList.get(position).getType().equals("text")){

                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Message?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                deleteReceivedMessage(position,messageViewHolder);
                            }
                        }
                    });
                    builder.show();


                }
                else if(userMessagesList.get(position).getType().equals("image")){

                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete",
                                    "Open Image",
                                    "Cancel"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Image?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == 0){
                                deleteReceivedMessage(position,messageViewHolder);
                            }
                            else if(i == 1){
                                Intent intent = new Intent(messageViewHolder.itemView.getContext(),ImageViewerActivity.class);
                                intent.putExtra("url",userMessagesList.get(position).getMessage());
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();


                }
            }
        });
    }

        //notifyDataSetChanged();


    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReceivedMessage(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               if(task.isSuccessful()){
                   rootRef.child("Messages")
                           .child(userMessagesList.get(position).getFrom())
                           .child(userMessagesList.get(position).getTo())
                           .child(userMessagesList.get(position).getMessageID())
                           .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                         if (task.isSuccessful()){
                             Toast.makeText(holder.itemView.getContext(), "Message Deleted!", Toast.LENGTH_SHORT).show();
                         }
                       }
                   });

                   Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(holder.itemView.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}



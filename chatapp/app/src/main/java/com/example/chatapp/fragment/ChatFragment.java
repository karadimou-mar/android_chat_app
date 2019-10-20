package com.example.chatapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.chatapp.activity.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatList;
    private DatabaseReference chatRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    //private String retrieved_image = "default_image";


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chat, container, false);

        chatList = privateChatView.findViewById(R.id.chat_list_recyclerview);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");



        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Contacts model) {

                        final String users_ID = getRef(position).getKey();
                        final String[] retrieved_image = {"default_image"};

                        userRef.child(users_ID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){

                                        retrieved_image[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retrieved_image[0]).into(holder.profileImage);
                                    }

                                    final String retrieved_name = dataSnapshot.child("name").getValue().toString();
                                    final String RETRIEVED_STATUS = dataSnapshot.child("status").getValue().toString();

                                    holder.username.setText(retrieved_name);

                                    if (dataSnapshot.child("user_status").hasChild("state")){

                                        String state = dataSnapshot.child("user_status").child("state").getValue().toString();
                                        String date = dataSnapshot.child("user_status").child("date").getValue().toString();
                                        String time = dataSnapshot.child("user_status").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            holder.userstatus.setText("online");

                                        }
                                        else if(state.equals("offline")){
                                            holder.userstatus.setText("offline");
                                            holder.userstatus.setText("Last Seen: " + date + " " + time);
                                        }

                                    }
                                    else{
                                        holder.userstatus.setText("offline");
                                    }


                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_userID", users_ID);
                                            chatIntent.putExtra("visit_username", retrieved_name);
                                            chatIntent.putExtra("visit_image", retrieved_image[0]);
                                            startActivity(chatIntent);
                                        }
                                    });

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        return new ChatViewHolder(view);

                    }
                };

        chatList.setAdapter(adapter);
        adapter.startListening();
        //adapter.notifyDataSetChanged();



    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

       CircleImageView profileImage;
       TextView username, userstatus;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            username = itemView.findViewById(R.id.user_profile_username_textview);
            userstatus = itemView.findViewById(R.id.user_status_textview);

        }
    }
}

package com.example.chatapp.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
public class ContactFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsRecyclerView;
    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        contactsView =  inflater.inflate(R.layout.fragment_contact, container, false);


        contactsRecyclerView = contactsView.findViewById(R.id.contacts_recyclerview);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return contactsView;
    }

    //afou exoun ginei init pio panw, mporw mesw tou firebase recycleradapter na kanw retrieve oles tis epafes apo to database
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view  = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.users_display_layout, viewGroup, false );

                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final Contacts model) {

                        final String USERS_IDS = getRef(position).getKey();
                        usersRef.child(USERS_IDS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                              if (dataSnapshot.exists()){

                                  if (dataSnapshot.child("user_status").hasChild("state")){

                                      String state = dataSnapshot.child("user_status").child("state").getValue().toString();
//                                      String date = dataSnapshot.child("user_status").child("date").getValue().toString();
//                                      String time = dataSnapshot.child("user_status").child("time").getValue().toString();

                                      if (state.equals("online")){
                                         holder.onlineIcon.setVisibility(View.VISIBLE);

                                      }
                                      else if(state.equals("offline")){
                                          holder.onlineIcon.setVisibility(View.INVISIBLE);
                                      }

                                  }
                                  else{
                                      holder.onlineIcon.setVisibility(View.INVISIBLE);
                                  }

                                  if (dataSnapshot.hasChild("image"))
                                  {
                                      String userImage = dataSnapshot.child("image").getValue().toString();
                                      String username = dataSnapshot.child("name").getValue().toString();
                                      String status = dataSnapshot.child("status").getValue().toString();

                                      //display them
                                      holder.username.setText(username);
                                      holder.userstatus.setText(status);
                                      Picasso.get().load(userImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);
                                  }
                                  else
                                  {
                                      String status = dataSnapshot.child("status").getValue().toString();
                                      String username = dataSnapshot.child("name").getValue().toString();

                                      holder.username.setText(username);
                                      holder.userstatus.setText(status);
                                  }
                              }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                };

        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_username_textview);
            userstatus = itemView.findViewById(R.id.user_status_textview);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.online_status_icon);
        }
    }

}

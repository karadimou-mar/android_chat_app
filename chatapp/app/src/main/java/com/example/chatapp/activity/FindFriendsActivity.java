package com.example.chatapp.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.chatapp.R;
import com.example.chatapp.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference usersRef;
//    private FirebaseAuth mAuth;
//    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        initFields();
    }

    //by using firebase recycler adapter we can retrieve the users from database
    @Override
    protected void onStart() {
        super.onStart();

//        mAuth = FirebaseAuth.getInstance();
//        currentUser = mAuth.getCurrentUser().getUid();


        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model) {


                        holder.username.setText(model.getName());
                        holder.userstatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);

                        //to itemView einai opoiodipote sto view mas
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String online_user_id = getRef(position).getKey();

                                Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("online_user_id", online_user_id);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.users_display_layout, viewGroup, false);

                        return new FindFriendsViewHolder(view);
                    }
                };
        findFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{

        TextView username, userstatus;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.user_profile_username_textview);
            userstatus = itemView.findViewById(R.id.user_status_textview);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }

    private void initFields() {
        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        findFriendsRecyclerList= findViewById(R.id.find_friends_recyclerview);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));
    }
}

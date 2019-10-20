package com.example.chatapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.chatapp.activity.GroupChatActivity;
import com.example.chatapp.R;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {

    private View groupFragmentView;
    private ListView group_listview;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> group_arrayList = new ArrayList<>();
    private DatabaseReference groupReference;


    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_group, container, false);

        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups");
        InitFields();
        retrieveAndDisplayGroups();

       group_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               String currentGroupName = adapterView.getItemAtPosition(position).toString();
               //send user to apropriate activity (sto group activity alla se auto pou clickare sugekrimena)
               Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
               groupChatIntent.putExtra("group name", currentGroupName);
               startActivity(groupChatIntent);
           }
       });

        return groupFragmentView;
    }

    private void retrieveAndDisplayGroups() {
        groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while(iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                group_arrayList.clear();
                group_arrayList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitFields() {
        group_listview = groupFragmentView.findViewById(R.id.groups_listview);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, group_arrayList);
        group_listview.setAdapter(arrayAdapter);
    }

}

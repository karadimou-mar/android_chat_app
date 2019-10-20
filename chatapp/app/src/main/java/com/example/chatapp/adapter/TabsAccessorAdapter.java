package com.example.chatapp.adapter;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.example.chatapp.fragment.ChatFragment;
import com.example.chatapp.fragment.ContactFragment;
import com.example.chatapp.fragment.GroupFragment;
import com.example.chatapp.fragment.RequestsFragment;

public class TabsAccessorAdapter extends FragmentPagerAdapter {


    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {

        switch (i)
        {
            case 0:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1:
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;
            case 2:
                ContactFragment contactFragment = new ContactFragment();
                return contactFragment;

            case 3:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

                default:
                    return null;
        }



    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
               return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Requests";

            default:
                return null;
        }
    }
}

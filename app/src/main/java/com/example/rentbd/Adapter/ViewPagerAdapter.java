package com.example.rentbd.Adapter;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.rentbd.Fragment.InfoFragment;
import com.example.rentbd.Fragment.UserPostFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public ViewPagerAdapter(Context mContext, FragmentManager fm){
        super(fm);
        context=mContext;

    }
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new InfoFragment();
        } else {
            return new UserPostFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "Information";
        }
        else {
            return "Your Post";
        }
        //        } else if (position == 1) {
//            return context.getString(R.string.monthly_title);
//        }
    }
}

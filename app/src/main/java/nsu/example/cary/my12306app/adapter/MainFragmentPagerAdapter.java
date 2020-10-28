package nsu.example.cary.my12306app.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import nsu.example.cary.my12306app.fragment.MyFragment;
import nsu.example.cary.my12306app.fragment.OrderFragment;
import nsu.example.cary.my12306app.fragment.TicketFragment;


public class MainFragmentPagerAdapter extends FragmentPagerAdapter {

    public String[] mTabs = {"车票","订单","@我的"};

    public MainFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new TicketFragment();
                break;

            case 1:
                fragment = new OrderFragment();
                break;

            case 2:
                fragment = new MyFragment();
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return mTabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs[position];
    }
}

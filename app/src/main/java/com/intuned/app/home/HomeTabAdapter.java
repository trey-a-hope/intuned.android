package com.intuned.app.home;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class HomeTabAdapter extends FragmentStatePagerAdapter {
    protected int numberOfTabs;

    protected TimeLineFragmentController timeLineFragmentController = new TimeLineFragmentController();
    protected NotificationsFragmentController notificationsFragmentController = new NotificationsFragmentController();

    public HomeTabAdapter(FragmentManager fragmentManager, int numberOfTabs) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return timeLineFragmentController;
            case 1:
                return notificationsFragmentController;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
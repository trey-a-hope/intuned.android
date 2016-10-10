package com.intuned.app.home;

import Adapters.VibeListAdapter;
import Enums.Emotion;

import android.app.DownloadManager;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import com.firebase.client.Query;
import com.intuned.app.R;
import com.intuned.app.authentication.SessionManager;

import Models.DomainModels.Song;
import Models.DomainModels.User;
import Listeners.RecyclerItemClickListener;
import Miscellaneous.SoundFile;
import Navigation.AppNavigator;
import Configuration.AppConfig;
import Services.FileService;
import Services.ModalService;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.TextView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;

public class HomeController extends AppCompatActivity {
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private TextView tvHeader;
    private ActionBar actionBar;
    private HomeTabAdapter homeTabAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initObjects();
        setValues();
        createTabLayout();
    }

    private void initUI() {
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nvView);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tvHeader = (TextView) findViewById(R.id.headerTV);
        viewPager = (ViewPager) findViewById(R.id.pager);
    }

    private void initObjects() {
        toolbar.setBackgroundColor(getResources().getColor(R.color.LightBlue900));
        setSupportActionBar(toolbar);
        setTitle("Vibes");
        actionBar = getSupportActionBar();
        drawerToggle = setupDrawerToggle();
    }

    private void setValues() {
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
        tvHeader.setText("Vibes, what are you listening to?");
        navigationView.setBackgroundColor(getResources().getColor(R.color.Indigo50));
        setupDrawerContent(navigationView);
    }

    private void createTabLayout() {
        //Create Time Line tab.
        Tab timeLineTab = tabLayout.newTab();
        timeLineTab.setIcon(R.drawable.ic_menu);
        tabLayout.addTab(timeLineTab);
        //Create Notifications tab.
        Tab notificationsTab = tabLayout.newTab();
        notificationsTab.setIcon(R.drawable.ic_menu);
        tabLayout.addTab(notificationsTab);
        //Set tabs background color.
        tabLayout.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.LightBlue800));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        homeTabAdapter = new HomeTabAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(homeTabAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {
            }

            @Override
            public void onTabReselected(Tab tab) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        AppNavigator.navigate(menuItem.getItemId(), this);

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.SharedPreferencesStorage;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{
    private static final int ACTIVITY_EDIT_EFFORT = 0;
    private static final int ACTIVITY_EDIT_SCHEDULE = 1;

    private ChoreList choreList;
    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences settings = getPreferences(0);

        storage = new SharedPreferencesStorage(
            settings,
            new SimpleChoreSelectorConverter(),
            new WeeklyEffortTrackerConverter());

        choreList = new ChoreList(
            storage,
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());

        choreList.load();

        GlobalState globalState = (GlobalState)getApplication();
        globalState.setChoreList(choreList);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        choreList.save();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_effort)
        {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreList.getEffortTracker();

            Intent intent = new Intent(this, EffortActivity.class);

            intent.putExtra("Monday", effortTracker.getMonday());
            intent.putExtra("Tuesday", effortTracker.getTuesday());
            intent.putExtra("Wednesday", effortTracker.getWednesday());
            intent.putExtra("Thursday", effortTracker.getThursday());
            intent.putExtra("Friday", effortTracker.getFriday());
            intent.putExtra("Saturday", effortTracker.getSaturday());
            intent.putExtra("Sunday", effortTracker.getSunday());

            startActivityForResult(intent, ACTIVITY_EDIT_EFFORT);

            return true;
        }

        if (id == R.id.activity_schedule)
        {
            choreList.save();

            String json = ContainerJsonConverter.toJson(storage.load());

            Intent intent = new Intent(this, ScheduleActivity.class);

            intent.putExtra("Schedule", json);

            startActivityForResult(intent, ACTIVITY_EDIT_SCHEDULE);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ACTIVITY_EDIT_EFFORT)
        {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreList.getEffortTracker();

            effortTracker.setMonday(data.getIntExtra("Monday", 0));
            effortTracker.setTuesday(data.getIntExtra("Tuesday", 0));
            effortTracker.setWednesday(data.getIntExtra("Wednesday", 0));
            effortTracker.setThursday(data.getIntExtra("Thursday", 0));
            effortTracker.setFriday(data.getIntExtra("Friday", 0));
            effortTracker.setSaturday(data.getIntExtra("Saturday", 0));
            effortTracker.setSunday(data.getIntExtra("Sunday", 0));
        }

        if (resultCode == RESULT_OK && requestCode == ACTIVITY_EDIT_SCHEDULE)
        {
            String json = data.getStringExtra("Schedule");

            Container container = ContainerJsonConverter.fromJson(
                json,
                new SimpleChoreSelectorConverter(),
                new WeeklyEffortTrackerConverter());

            storage.save(container);

            choreList.load();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Fragment fragment;
        Class<?> fragmentClass;

        switch (item.getItemId())
        {
            case R.id.nav_all_chores:
                fragmentClass = AllChores.class;
                break;
            case R.id.nav_todays_chores:
            default:
                fragmentClass = TodaysChores.class;
        }

        try
        {
            fragment = (Fragment)fragmentClass.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setTitle(item.getTitle());

        return true;
    }
}

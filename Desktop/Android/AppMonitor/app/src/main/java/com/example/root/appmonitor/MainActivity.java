package com.example.root.appmonitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private String[] FRAGMENT_NAME = {"Most Used", "Timeline", "Weekly Usage"/*, "Monthly Usage"*/};
    final Context context = this;
    SharedPreferences settings,prefs;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        if(!permissionGranted()){
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
            Toast.makeText(context, "Please enable usage access",Toast.LENGTH_LONG).show();
        }

        settings = getSharedPreferences("password", Context.MODE_APPEND);
        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);

        startService(new Intent(this,BlockingService.class));

        // Setting up Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);

        //Initialize ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        //Setup ViewPager Adapter
        setupViewPager(viewPager);
        //Tablayout initialization
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);
        //setup Listeners to Tabs
        tabLayout.setOnTabSelectedListener(this);
    }

    // This method will call Adapter for ViewPager
    private void setupViewPager(ViewPager viewPager) {
        PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MostUsedApps(), FRAGMENT_NAME[0]);
        adapter.addFragment(new TimelineFragment(),FRAGMENT_NAME[1]);
        adapter.addFragment(new WeeklyFragment(), FRAGMENT_NAME[2]);
        //adapter.addFragment(new MonthlyFragment(), FRAGMENT_NAME[3]);
        //Set adapter to ViewPager
        viewPager.setAdapter(adapter);
    }

    // These are the methods which handles Tab Selection, Unselection & Reselection
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        //Set the tab icon from selected array
        //tab.setIcon(SELECTED_ICON[tab.getPosition()]);
        //toolbar.setTitle(FRAGMENT_NAME[tab.getPosition()]);
        //When Tab is clicked this line set the viewpager to corresponding fragment
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // Set icon from unselected tab array
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:

                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.password_prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String pass = userInput.getText().toString();
                        String savedPassword = new String();
                        savedPassword = settings.getString("password",savedPassword);
                        Log.d("pass",pass);
                        if(pass.equals(savedPassword)){
                            Intent intent = new Intent(context,SettingsActivity.class);
                            startActivity(intent);
                        }
                        else{
                            final Snackbar snackbar = Snackbar.make(coordinatorLayout, "PIN incorrect", Snackbar.LENGTH_LONG);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.putString("password","1234");
            editor.commit();
            prefs.edit().putBoolean("firstrun", false).commit();
        }
    }

    public boolean permissionGranted(){
        final UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        final List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0,  System.currentTimeMillis());

        return !queryUsageStats.isEmpty();
    }
}
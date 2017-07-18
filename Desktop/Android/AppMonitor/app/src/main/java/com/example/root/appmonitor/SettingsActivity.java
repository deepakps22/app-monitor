package com.example.root.appmonitor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelySaveStateHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private LovelySaveStateHandler saveStateHandler;
    final Context context = this;
    SharedPreferences settings;
    private RelativeLayout coordinatorLayout;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        coordinatorLayout = (RelativeLayout) findViewById(R.id.activity_settings);

        settings = getSharedPreferences("password", Context.MODE_APPEND);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Settings");

        saveStateHandler = new LovelySaveStateHandler();

        final ArrayList<String> preferenceList = new ArrayList<String>();
        preferenceList.add("Select Apps to Monitor");
        preferenceList.add("Set Time Limit");
        preferenceList.add("Block Apps");
        preferenceList.add("Change PIN");

        ListView list = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,preferenceList);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView)view).getText().toString();
                //Toast.makeText(getBaseContext(), item, Toast.LENGTH_LONG).show();
                if(item.equals(preferenceList.get(0))){
                    showMultiChoiceDialog(savedInstanceState);
                }
                else if(item.equals("Set Time Limit")){
                    Intent intent = new Intent(view.getContext(),TImeLimitActivity.class);
                    startActivity(intent);
                }
                else if(item.equals("Block Apps")){
                    showBlockMultiChoiceDialog(savedInstanceState);
                }
                else{
                    showPasswordChangeDialog();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showMultiChoiceDialog(Bundle savedInstanceState) {
        List<String> appList = getInstalledApps();
        String[] items = new String[appList.size()];
        final SharedPreferences settings = getSharedPreferences("YourActivityPreferences", Context.MODE_APPEND);
        items = appList.toArray(items);
        Arrays.sort(items);
        boolean[] isSelected = new boolean[items.length];
        Set<String> appSet = new HashSet<String>();
        appSet = settings.getStringSet("appSet",appSet);
        int count = 0;
        for(String name : items){
            if(appSet.contains(name))
                isSelected[count] = true;
            count++;
        }
        new LovelyChoiceDialog(this, R.style.CheckBoxTintTheme)
                .setTopColorRes(R.color.primary_dark)
                .setTitle("Select Apps to Monitor")
                .setInstanceStateHandler(R.id.listView,saveStateHandler)
                .setItemsMultiChoice(items,isSelected,new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                    @Override
                    public void onItemsSelected(List<Integer> positions, List<String> items) {
                        Set<String> appSet = new HashSet<String>();
                        for(String app : items){
                            appSet.add(app);
                            Log.d("saveprefs",app);
                        }
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.putStringSet("appSet",appSet);
                        editor.commit();
                    }
                })
                .setConfirmButtonText("Save")
                .setSavedInstanceState(savedInstanceState)
                .show();
    }

    private void showBlockMultiChoiceDialog(Bundle savedInstanceState) {
        List<String> appList = getInstalledApps();
        String[] items = new String[appList.size()];
        final SharedPreferences settings = getSharedPreferences("BlockedApps", Context.MODE_APPEND);
        items = appList.toArray(items);
        Arrays.sort(items);
        boolean[] isSelected = new boolean[items.length];
        Set<String> appSet = new HashSet<String>();
        appSet = settings.getStringSet("blockedAppSet",appSet);
        int count = 0;
        for(String name : items){
            if(appSet.contains(name))
                isSelected[count] = true;
            count++;
        }
        new LovelyChoiceDialog(this, R.style.CheckBoxTintTheme)
                .setTopColorRes(R.color.primary_dark)
                .setTitle("Block Apps")
                .setInstanceStateHandler(R.id.listView,saveStateHandler)
                .setItemsMultiChoice(items,isSelected,new LovelyChoiceDialog.OnItemsSelectedListener<String>() {
                    @Override
                    public void onItemsSelected(List<Integer> positions, List<String> items) {
                        Set<String> appSet = new HashSet<String>();
                        for(String app : items){
                            appSet.add(app);
                            Log.d("saveprefs",app);
                        }
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.putStringSet("blockedAppSet",appSet);
                        editor.commit();
                    }
                })
                .setConfirmButtonText("Save")
                .setSavedInstanceState(savedInstanceState)
                .show();
    }

    private List<String> getInstalledApps() {
        List<String> res = new ArrayList<String>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            String appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
            res.add(appName);
        }
        return res;
    }

    private boolean isSystemPackage(PackageInfo pkgInfo) {
        return ((pkgInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && !pkgInfo.packageName.contains("google")) ? true : false;
    }

    private void showPasswordChangeDialog(){

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.password_change_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setView(promptsView);

        final EditText curr = (EditText) promptsView.findViewById(R.id.currentPIN);
        final EditText newp = (EditText) promptsView.findViewById(R.id.newPIN);
        final EditText confirm = (EditText) promptsView.findViewById(R.id.confirmPIN);

        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String currPIN = curr.getText().toString();
                String newPIN = newp.getText().toString();
                String confirmPIN = confirm.getText().toString();

                String savedPassword = new String();
                savedPassword = settings.getString("password",savedPassword);

                if(currPIN.equals(savedPassword)){
                    if(newPIN.equals(confirmPIN)){
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.putString("password",newPIN);
                        editor.commit();
                        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "PIN Saved", Snackbar.LENGTH_LONG);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                    else{
                        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "PIN Mismatch", Snackbar.LENGTH_LONG);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                }
                else{
                    final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Current PIN is Mismatch", Snackbar.LENGTH_LONG);
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
    }
}
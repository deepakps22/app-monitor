package com.example.root.appmonitor;

/**
 * Created by root on 7/4/17.
 */

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlockingService extends Service {
    String topPackage;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onCreate();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        topPackage = new String();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SharedPreferences settings = getSharedPreferences("BlockedApps", Context.MODE_APPEND);
                    Set<String> appSet = new HashSet<String>();
                    appSet = settings.getStringSet("blockedAppSet",appSet);
                    String current = getTopPackage();
                    if(!topPackage.equals(current)){
                        topPackage = current;
                        Log.d("top_package",topPackage);
                        if(appSet.contains(getAppName(topPackage))){
                            Intent i = new Intent(BlockingService.this,BlockActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("packageName",topPackage);
                            startActivity(i);
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {

    }

    String getTopPackage(){
        long ts = System.currentTimeMillis();
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts-1000, ts);
        if (usageStats == null || usageStats.size() == 0) {
            return "";
        }
        Collections.sort(usageStats, new RecentUseComparator());
        return usageStats.get(0).getPackageName();
    }

    private String getAppName(String packageName) {
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
        String appName = new String();
        for (int i = 0; i < packs.size(); i++) {
            PackageInfo p = packs.get(i);
            if(p.packageName.equals(packageName)){
                appName = p.applicationInfo.loadLabel(getPackageManager()).toString();
            }
        }
        return appName;
    }

    static class RecentUseComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }
}

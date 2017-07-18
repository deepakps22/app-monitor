package com.example.root.appmonitor;

/**
 * Created by root on 1/3/17.
 */

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alorma.timeline.TimelineView;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class TimelineFragment extends Fragment {

    ArrayList<Events> items;
    ListView list;
    SharedPreferences settings;
    public TimelineFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.timeline_fragment, container, false);
        list = (ListView)rootView.findViewById(R.id.list);
        settings = rootView.getContext().getSharedPreferences("YourActivityPreferences", Context.MODE_APPEND);
//        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//        startActivity(intent);
        if(items != null)
            items.clear();
        LoadTimeline task = new LoadTimeline();
        task.execute();
        // Inflate the layout for this fragment
        return rootView;
    }

    public ArrayList<Events> getStats() {
        UsageStatsManager usm = (UsageStatsManager)getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(), time);
        SortedMap<Long,UsageStats> mySortedMap = new TreeMap<>();
        if(appList != null) {
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
            }
        }
        ArrayList<Events> timeLineList = new ArrayList<>();
        Set<String> appSet = new HashSet<String>();
        appSet = settings.getStringSet("appSet",appSet);

        for(Map.Entry<Long,UsageStats> entry : mySortedMap.entrySet()){
            String pName = entry.getValue().getPackageName();
            String appName = getApplicationName(pName);
            Events tempEvent = new Events(appName+" "+getTime(entry.getValue().getLastTimeUsed()),pName,TimelineView.TYPE_MIDDLE);
            if(appSet.contains(appName))
                timeLineList.add(tempEvent);
        }
        Collections.reverse(timeLineList);
        if(timeLineList.size() > 0){
            timeLineList.get(0).setAlignment(TimelineView.ALIGNMENT_START);
            timeLineList.get(timeLineList.size()-1).setAlignment(TimelineView.ALIGNMENT_END);
        }
        return timeLineList;
    }

    public boolean checkSystemApp(String packageName) throws PackageManager.NameNotFoundException {
        PackageManager pm = getActivity().getPackageManager();
        ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
        if((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }
        return false;
    }

    String getApplicationName(String packageName){
        final PackageManager pm = getActivity().getPackageManager();
        ApplicationInfo ai;
        try{
            ai = pm.getApplicationInfo(packageName, 0);
        }catch(final PackageManager.NameNotFoundException e){
            ai = null;
        }
        String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
        return applicationName;
    }

    public String getTime(long millis){
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
        Date resultdate = new Date(millis);
        return resultdate.getHours()+":"+ resultdate.getMinutes();
    }

    private class LoadTimeline extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            items = getStats();
            return "data";
        }

        @Override
        protected void onPostExecute(String result) {
            if(getContext() != null && items != null)
                list.setAdapter(new EventsAdapter(getContext(), items));
        }
    }
}

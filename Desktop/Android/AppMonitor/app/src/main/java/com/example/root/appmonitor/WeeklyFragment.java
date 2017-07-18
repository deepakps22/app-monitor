package com.example.root.appmonitor;

/**
 * Created by root on 1/3/17.
 */

import android.app.ProgressDialog;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeeklyFragment extends Fragment {

    SharedPreferences settings;
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();
    HorizontalBarChart barChart;
    private ProgressDialog progressDialog;
    public WeeklyFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.weekly_fragment, container, false);
        settings = rootView.getContext().getSharedPreferences("YourActivityPreferences", Context.MODE_APPEND);
        barChart = (HorizontalBarChart)rootView.findViewById(R.id.barchart);
        labels.clear();
        labels.add("Sunday");
        labels.add("Saturday");
        labels.add("Friday");
        labels.add("Thursday");
        labels.add("Wednesday");
        labels.add("Tuesday");
        labels.add("Monday");
        entries.clear();

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.isIndeterminate();
        progressDialog.setMessage("Loading Usage Stats");

        LoadGraphData task = new LoadGraphData();
        task.execute();
        // Inflate the layout for this fragment
        return rootView;
    }

    public float getData(long millis){
        //noinspection ResourceType
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getActivity().getSystemService("usagestats");
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,millis,millis+86400000);
        float usageHours = 0;
        Set<String> appSet = new HashSet<String>();
        appSet = settings.getStringSet("appSet",appSet);
        for(UsageStats us : stats){
            if(appSet.contains(getName(us.getPackageName())))
                usageHours += (us.getTotalTimeInForeground()*1.0)/(1000*60*60);
        }
        return usageHours;
    }

    private class LoadGraphData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            long millis = cal.getTimeInMillis();
            int dayCount = 0;
            while(millis <= System.currentTimeMillis()){
                entries.add(new BarEntry(getData(millis),6-dayCount));
                millis += 86400000;
                dayCount++;
            }
            return "data";
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            BarDataSet bardataset = new BarDataSet(entries, "Hours");
            bardataset.setColor(ColorTemplate.rgb("#FF4081"));
            BarData data = new BarData(labels, bardataset);
            barChart.setData(data);

            barChart.setDescription("Weekly Usage");
            barChart.animateY(2000);
        }
    }

    String getName(String packageName){
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
}
package com.example.root.appmonitor;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Created by root on 1/3/17.
 */

public class MostUsedApps extends Fragment {
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("reply");
    private RecyclerView recyclerView;
    private AppPackageAdapter adapter;
    private List<AppPackage> albumList;
    SharedPreferences settings;
    Set<String> appSet;

    public MostUsedApps() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.most_used_fragment, container, false);

        settings = rootView.getContext().getSharedPreferences("YourActivityPreferences", Context.MODE_APPEND);
        appSet = new HashSet<String>();
        appSet = settings.getStringSet("appSet",appSet);
        for(String app : appSet){
            Log.d("prefs",app);
        }
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        albumList = new ArrayList<>();
        adapter = new AppPackageAdapter(getContext(), albumList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getContext(),2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        albumList.clear();
        LoadCards task = new LoadCards();
        task.execute();

//        Button b = (Button) rootView.findViewById(R.id.button2);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText e = (EditText) rootView.findViewById(R.id.editText);
//                String str_val = e.getText().toString();
//                String id = myRef.push().getKey();
//                myRef.child(id).setValue(str_val);
//            }
//        });
        // Inflate the layout for this fragment
        return rootView;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
//                    //getting artist
//                    String fetch = postSnapshot.getValue(String.class);
//                    //adding artist to the list
//                    Log.d("fetch",fetch);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    /**
     * Adding few albums for testing
     */
    private void prepareAlbums() {
        int[] covers = new int[]{
                R.drawable.avatar,
                R.mipmap.ic_launcher};

        long TimeInforground = 500;
        int minutes = 500, seconds = 500, hours = 500;
        String PackageName = new String();
        Map<String,UsageStats> usageMap = getUsageTimes();
        for(Map.Entry<String,UsageStats> entry : usageMap.entrySet()){
            TimeInforground = entry.getValue().getTotalTimeInForeground();
            PackageName = entry.getKey();
            minutes = (int) ((TimeInforground / (1000 * 60)) % 60);
            seconds = (int) (TimeInforground / 1000) % 60;
            hours = (int) ((TimeInforground / (1000 * 60 * 60)) % 24);
            if((minutes != 0 || seconds != 0 || hours != 0) && !getName(PackageName).contains("unknown") && appSet.contains(getName(PackageName))){
                Log.d("TAG",getName(PackageName)+" "+hours+"h:"+minutes+"m:"+seconds+"s");
                albumList.add(new AppPackage(getName(PackageName),hours,minutes,seconds,getIcon(PackageName)));
            }
        }
        Collections.sort(albumList);
        Collections.reverse(albumList);
        Log.d("size",albumList.size()+"");
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public Map<String,UsageStats> getUsageTimes(){
        //noinspection ResourceType
        UsageStatsManager mUsageStatsManager = (UsageStatsManager)getActivity().getSystemService("usagestats");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long time = System.currentTimeMillis();
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,cal.getTimeInMillis(), time);
        ArrayMap<String, UsageStats> aggregatedStats = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats newStat = stats.get(i);
            UsageStats existingStat = aggregatedStats.get(newStat.getPackageName());
            if (existingStat == null) {
                aggregatedStats.put(newStat.getPackageName(), newStat);
            } else {
                existingStat.add(newStat);
            }
        }
        return aggregatedStats;
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

    public Drawable getIcon(String packageName){
        try {
            return getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private class LoadCards extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            prepareAlbums();
            return "data";
        }

        @Override
        protected void onPostExecute(String result) {
            adapter.notifyDataSetChanged();
        }
    }
}
package com.example.root.appmonitor;

/**
 * Created by root on 2/3/17.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alorma.timeline.RoundTimelineView;
import com.alorma.timeline.TimelineView;

import java.util.List;

class EventsAdapter extends ArrayAdapter<Events> {
    private final LayoutInflater layoutInflater;

    public EventsAdapter(Context context, List<Events> objects) {
        super(context, 0, objects);
        layoutInflater = LayoutInflater.from(context);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_main, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.text = (TextView) convertView.findViewById(R.id.textView);
            viewHolder.timeline = (RoundTimelineView) convertView.findViewById(R.id.timeline);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        Events events = getItem(position);

        viewHolder.text.setText(events.getName());
        viewHolder.timeline.setTimelineType(events.getType());
        viewHolder.timeline.setTimelineAlignment(events.getAlignment());
        return convertView;
    }

    static class ViewHolderItem {
        TextView text;
        TimelineView timeline;
    }
    public Drawable getIcon(String packageName){
        try {
            return getContext().getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
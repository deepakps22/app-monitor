package com.example.root.appmonitor;

/**
 * Created by root on 2/3/17.
 */

import android.support.annotation.NonNull;

import com.alorma.timeline.TimelineView;

class Events {
    private String name;
    private int type;
    private int alignment;
    private String packageName;
    public Events(@NonNull String name,String packageName) {
        this(name,packageName, TimelineView.TYPE_DEFAULT);
    }

    public Events(@NonNull String name,String packageName, int type) {
        this(name,packageName, type, TimelineView.ALIGNMENT_DEFAULT);
    }

    public Events(@NonNull String name,String packageName, int type, int alignment) {
        this.name = name;
        this.packageName = packageName;
        this.type = type;
        this.alignment = alignment;
    }

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public @TimelineView.TimelineType int getType() {
        return type;
    }

    public void setType(@TimelineView.TimelineType int type) {
        this.type = type;
    }

    public @TimelineView.TimelineAlignment int getAlignment() {
        return alignment;
    }

    public void setAlignment(@TimelineView.TimelineAlignment int alignment) {
        this.alignment = alignment;
    }

    public String getPackageName(){
        return this.packageName;
    }

    public void setPackageName(String packageName){
        this.packageName = packageName;
    }
    @Override public String toString() {
        return "Events{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", alignment=" + alignment +
                '}';
    }
}
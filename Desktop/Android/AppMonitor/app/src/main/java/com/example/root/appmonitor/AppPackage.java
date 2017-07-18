package com.example.root.appmonitor;

import android.graphics.drawable.Drawable;

/**
 * Created by root on 7/3/17.
 */

public class AppPackage implements Comparable{
    private String name;
    int hours,minutes,seconds;
    Drawable thumbnail;

    public AppPackage() {
    }

    public AppPackage(String name,int hours,int minutes,int seconds,Drawable thumbnail) {
        this.name = name;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime(){
        return hours+"h:"+minutes+"m:"+seconds+"s";
    }

    public Drawable getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public int compareTo(Object o) {
        AppPackage ap = (AppPackage) o;
        if(this.hours < ap.hours){
            return -1;
        }
        else if(this.hours > ap.hours){
            return 1;
        }
        else{
            if(this.minutes < ap.minutes){
                return -1;
            }
            else if(this.minutes > ap.minutes){
                return 1;
            }
            else{
                if(this.seconds < ap.seconds){
                    return -1;
                }
                else{
                    return 1;
                }
            }
        }
    }
}

package com.indrajit.myplaces;


import com.google.android.gms.maps.model.LatLng;

class MyLocation {

    private String fullname, nickname;
    private int fav;
    private int priority;
    private double lat,lon;

    MyLocation(String fullname, String nickname, int fav, int priority, double lat, double lon) {
        this.fullname = fullname;
        this.nickname = nickname;
        this.fav = fav;
        this.priority = priority;
        this.lat = lat;
        this.lon = lon;
    }

    String getFullname() {
        return fullname;
    }

    String getNickname() {
        return nickname;
    }

    int getFav() {
        return fav;
    }

    int getPriority() {
        return priority;
    }

    double getLat() {
        return lat;
    }

    double getLon() {
        return lon;
    }

    LatLng getLatLng(){

        return new LatLng(lat, lon);
    }
}

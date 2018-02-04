package com.indrajit.myplaces;

import com.google.android.gms.maps.model.LatLng;

class LatLngInt {

    private LatLng l;
    private Integer i;

    LatLngInt(LatLng l, Integer i){

        this.l = l;
        this.i = i;
    }

    Integer getI(){

        return i;
    }

    LatLng getL(){

        return l;
    }
}

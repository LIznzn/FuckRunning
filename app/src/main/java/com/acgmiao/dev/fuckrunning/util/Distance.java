package com.acgmiao.dev.fuckrunning.util;

import com.google.android.gms.maps.model.LatLng;

public class Distance {

    private static final double EARTH_RADIUS = 6378137;

    //根据两点坐标，计算两点间距离，单位为m
    public static double GetDistance(LatLng location1, LatLng location2) {
        double lat1 = location1.latitude;
        double lng1 = location1.longitude;
        double lat2 = location2.latitude;
        double lng2 = location2.longitude;
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

}

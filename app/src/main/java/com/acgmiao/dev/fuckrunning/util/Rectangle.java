package com.acgmiao.dev.fuckrunning.util;

import com.google.android.gms.maps.model.LatLng;

public class Rectangle {
    private double West;
    private double North;
    private double East;
    private double South;

    private Rectangle(double latitude1, double longitude1, double latitude2, double longitude2) {
        this.West = Math.min(longitude1, longitude2);
        this.North = Math.max(latitude1, latitude2);
        this.East = Math.max(longitude1, longitude2);
        this.South = Math.min(latitude1, latitude2);
    }

    private static Rectangle[] region = new Rectangle[]{
            new Rectangle(49.220400, 79.446200, 42.889900, 96.330000),
            new Rectangle(54.141500, 109.687200, 39.374200, 135.000200),
            new Rectangle(42.889900, 73.124600, 29.529700, 124.143255),
            new Rectangle(29.529700, 82.968400, 26.718600, 97.035200),
            new Rectangle(29.529700, 97.025300, 20.414096, 124.367395),
            new Rectangle(20.414096, 107.975793, 17.871542, 111.744104),

    };

    private static Rectangle[] exclude = new Rectangle[]{
            new Rectangle(25.398623, 119.921265, 21.785006, 122.497559),
            new Rectangle(22.284000, 101.865200, 20.098800, 106.665000),
            new Rectangle(21.542200, 106.452500, 20.487800, 108.051000),
            new Rectangle(55.817500, 109.032300, 50.325700, 119.127000),
            new Rectangle(55.817500, 127.456800, 49.557400, 137.022700),
            new Rectangle(44.892200, 131.266200, 42.569200, 137.022700),
    };

    public static boolean IsInsideChina(LatLng location) {
        for (int i = 0; i < region.length; i++) {
            if (InRectangle(region[i], location)) {
                for (int j = 0; j < exclude.length; j++) {
                    if (InRectangle(exclude[j], location)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static boolean InRectangle(Rectangle rect, LatLng location) {
        return rect.West <= location.longitude
                && rect.East >= location.longitude
                && rect.North >= location.latitude
                && rect.South <= location.latitude;
    }
}
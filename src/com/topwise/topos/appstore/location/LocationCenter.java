package com.topwise.topos.appstore.location;

import com.topwise.topos.appstore.conn.behavior.BehaviorLogManager;
import com.topwise.topos.appstore.conn.behavior.LocationInfo2;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Locale;

public class LocationCenter {

    private static LocationCenter mThis = null;

    private LocationManager mLocMgr = null;

    private Location mLocation = new Location(LocationManager.NETWORK_PROVIDER);
    private Address mAddress = new Address(Locale.getDefault());

    public static LocationCenter getInstance() {
        if (mThis == null) {
            synchronized (LocationCenter.class) {
                if (mThis == null) {
                    mThis = new LocationCenter();
                }
            }
        }
        return mThis;
    }

    public void init(Context context) {
        mLocMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocation() {
        try {
            mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 6*60*60*1000, 10*1000, locationListener);
        } catch (Exception e) {
        }
    }
    
    public void stopLocation() {
        try {
            mLocMgr.removeUpdates(locationListener);
        } catch (Exception e) {
        }
    }

    public Location getLocation() {
        return mLocation;
    }

    public Address getAddress() {
        return mAddress;
    }
    
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocation = location;
            LocationInfo2.setLocationInfo("", "", location.getLongitude(), location.getLatitude());
            BehaviorLogManager.getInstance().location2Behavior(LocationInfo2.getLocationInfo());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}

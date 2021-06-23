package com.mentalist.uberclone.providers;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeoFireProvider {
    private DatabaseReference mDataBase;
    private GeoFire mGeoFire;

    public GeoFireProvider(){
        mDataBase = FirebaseDatabase.getInstance().getReference().child("active_drivers");
        mGeoFire = new GeoFire(mDataBase);

    }

    public void saveLocation(String idDriver, LatLng latLng){
        mGeoFire.setLocation(idDriver,new GeoLocation(latLng.latitude, latLng.longitude));

    }

    public void removeLocation(String idDriver){
        mGeoFire.removeLocation(idDriver);
    }

    public GeoQuery getActiveDrivers(LatLng latLng, double radius){
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(latLng.latitude,latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return  geoQuery;
    }
}

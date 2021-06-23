package com.mentalist.uberclone.activities.client;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
import com.mentalist.uberclone.R;
import com.mentalist.uberclone.activities.MainActivity;
import com.mentalist.uberclone.activities.driver.MapDriverActivity;
import com.mentalist.uberclone.includes.MyToolbar;
import com.mentalist.uberclone.models.FCMBody;
import com.mentalist.uberclone.models.FCMResponse;
import com.mentalist.uberclone.providers.AuthProvider;
import com.mentalist.uberclone.providers.GeoFireProvider;
import com.mentalist.uberclone.providers.NotificationProvider;
import com.mentalist.uberclone.providers.TokenProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientActivity extends AppCompatActivity implements OnMapReadyCallback {

    private AuthProvider mAuthProvider;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;

    private Marker mMarker;

    private LatLng mCurrentLatLng;

    private double mRadius = 0.6;

    private double initialDistance = 500;

    private GeoFireProvider mGeoFireProvider;
    private TokenProvider mTokenProvider;

    private List<Marker> mDriversMarkers = new ArrayList<>();

    private boolean mISFirstTime = true;

    private PlacesClient mPlaces;
    private AutocompleteSupportFragment mAutoComplete;
    private AutocompleteSupportFragment mAutoCompleteDestination;

    private String mOrigin;
    private LatLng mOriginLatLog;

    private String mDestination;
    private LatLng mDestinationLatLog;

    private GoogleMap.OnCameraIdleListener mCameraListener;
    private Button mButtonRequestDriver ;

    private TextView textViewLocationClientCurrent;

    private NotificationProvider mNotificationProvider;

    private double limiteSuperior = 500, limiteInferior = 400, distanciaAntes, distanciaActual;
    private String timeToHome;
    private boolean notificationON = true;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {

                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //comentamos para que se pueda seleccionar el origen con el icono image view

                    if(mMarker != null){
                        mMarker.remove();
                    }

                    mMarker = mMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Tu posición")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.casa_cliente_100x100))
                    );
                    //Obtener la ubicacion del usuario en tiempo real
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(15f) //Altura de vista por defecto
                                    .build()
                    ));

                    if (mISFirstTime) {
                        mISFirstTime = false;
                        getActiveDrivers();
                        limitSearch();
                    }
                }
            }
        }
    };

    private void instanceAutoCompleteOrigin(){
        mAutoComplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteOrigin);
        mAutoComplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoComplete.setHint("Lugar de recogida");
        mAutoComplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLog = place.getLatLng();
                Log.d("PLACE", "Name" + mOrigin);
                Log.d("PLACE", "Lat" + mOriginLatLog.latitude);
                Log.d("PLACE", "Lng" + mOriginLatLog.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void instanceAutoCompleteDestination(){
        mAutoCompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestino);
        mAutoCompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutoCompleteDestination.setHint("Lugar de destino");
        mAutoCompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatLog = place.getLatLng();
                Log.d("PLACE", "Name" + mDestination);
                Log.d("PLACE", "Lat" + mDestinationLatLog.latitude);
                Log.d("PLACE", "Lng" + mDestinationLatLog.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void onCameraMove(){
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapClientActivity.this);
                    mOriginLatLog = mMap.getCameraPosition().target;
                    List<Address> adressList = geocoder.getFromLocation(mOriginLatLog.latitude, mOriginLatLog.longitude,1);
                    String city = adressList.get(0).getLocality();
                    String country = adressList.get(0).getCountryName();
                    String address = adressList.get(0).getAddressLine(0);
                    mOrigin = address + " " + city;
                    mAutoComplete.setText(address + " " + city);

                }catch (Exception e){
                    Log.d("Error mCamera Listener" ,"Mensaje de error" + e.getMessage());
                }
            }
        };
    }

    private void limitSearch(){
        //LIMITE DE DISTANCIA 5 KM
        LatLng northSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000,0);
        LatLng southSide = SphericalUtil.computeOffset(mCurrentLatLng, 5000,180);
        mAutoComplete.setCountry("ECU");
        mAutoComplete.setLocationBias(RectangularBounds.newInstance(southSide, northSide));

        mAutoCompleteDestination.setCountry("ECU");
        mAutoCompleteDestination.setLocationBias(RectangularBounds.newInstance(southSide, northSide));

    }


    private void getActiveDrivers() {

        mGeoFireProvider.getActiveDrivers(mCurrentLatLng,mRadius).addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                //AÑADIR MARCADORES DE LOS CONDUCTORES DE LA APP
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(dataSnapshot.getKey())) { //aqui puede haber un problema
                            return;
                        }
                    }
                }

                LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(driverLatLng)
                        .title("Camión Recolector")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.camion_recolector_100x100))
                );
                marker.setTag(dataSnapshot.getKey());
                mDriversMarkers.add(marker);

                double distanceBetween = getDistanceBetween(mCurrentLatLng,driverLatLng);

                double distanceHome = distanceBetween ;
                double time = distanceBetween / 100;
                String textTime = time + " minutos";
                String textDistanceHome  = distanceHome + " metros";
               // sendNotification(textDistanceHome , textTime);
                distanciaActual = getDistanceBetween(mCurrentLatLng,driverLatLng);
                notificationON = true;


            }

            //CUANDO UN CONDUCTOR SE DESCONECTA
            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(dataSnapshot.getKey())) { //aqui puede haber un problema
                            marker.remove();
                            mDriversMarkers.remove(marker);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
                //ACTUALIZAR LA POSICIÓN DE CADA CONDUCTOR
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(dataSnapshot.getKey())) { //aqui puede haber un problema
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                            LatLng driverLatLng = new LatLng(location.latitude, location.longitude);
                            Toast.makeText(MapClientActivity.this, "Distancia actual:" + distanciaActual, Toast.LENGTH_SHORT).show();
                            distanciaAntes = distanciaActual;
                            distanciaAntes = Math.round(distanciaAntes*100.0)/100.0;
                            distanciaActual = getDistanceBetween(mCurrentLatLng,driverLatLng);
                            distanciaActual = Math.round(distanciaActual*100.0)/100.0;
                            timeToHome = "" + (int) (distanciaActual / 167);
                            textViewLocationClientCurrent.setText("DAC: " +  distanciaActual + " DAN: " + distanciaAntes + " LS: " + limiteSuperior + " LI: " + limiteInferior);
                            if(distanciaAntes >= distanciaActual){
                                if(distanciaActual <= limiteSuperior && distanciaActual>= limiteInferior){
                                    if(notificationON) {
                                        //Toast.makeText(MapClientActivity.this, "Notificación!!! \n El camion se encuentra a " + distanciaActual + " metros" + " aproximadamente a " + timeToHome + " de tu domicilio", Toast.LENGTH_LONG).show();
                                        sendNotification("" + distanciaActual,timeToHome);
                                        limiteSuperior = limiteSuperior - 100;
                                        limiteInferior = limiteInferior - 100;
                                        if(limiteSuperior == 0){
                                            notificationON = false;
                                        }
                                    }
                                }
                            }else if(distanciaActual >= 550){
                                limiteSuperior = 500;
                                limiteInferior = 400;
                                notificationON = true;
                            }
                            return;
                        }
                    }
                }

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_client);


        MyToolbar.show(this, "Tu domicilio (Posición actual)", false);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mAuthProvider = new AuthProvider();
        mGeoFireProvider = new GeoFireProvider();
        mTokenProvider = new TokenProvider();

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);

        mButtonRequestDriver = findViewById(R.id.btnRequestDriver);
        textViewLocationClientCurrent = findViewById(R.id.textViewLocationClientCurrent);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }

        mPlaces = Places.createClient(this);

        instanceAutoCompleteOrigin();
        instanceAutoCompleteDestination();
        onCameraMove();


        mButtonRequestDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDriver();
            }
        });


        generateToken();

        textViewLocationClientCurrent.setText("Gracias por activar nuestro servicio \nTe notificaremos cuando nuestro recolector este cerca de tu posición");

        mNotificationProvider = new NotificationProvider();

    }



    private void requestDriver(){
        if(mOriginLatLog != null && mDestinationLatLog != null){
            Intent intent = new Intent(MapClientActivity.this, DetailRequestActivity.class);
            intent.putExtra("origin_lat" , mOriginLatLog.latitude);
            intent.putExtra("origin_lng",mOriginLatLog.longitude);
            intent.putExtra("destination_lat",mDestinationLatLog.latitude);
            intent.putExtra("destination_lng",mDestinationLatLog.longitude);
            intent.putExtra("origin",mOrigin);
            intent.putExtra("destination",mDestination);
            startActivity(intent);
        }else{
            Toast.makeText(this, "Debe seleccionar el lugar de destino y recogida", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    } else {
                        showAlertDialogNOGPS();
                    }
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }


    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos para continuar")
                        .setMessage("Esta aplicacion requiere de los permisos para poder utilizarse")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapClientActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);

            }
        }

    }

    private void showAlertDialogNOGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa tu ubicación para continuar")
                .setPositiveButton("Configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNOGPS();
        }
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogNOGPS();
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                showAlertDialogNOGPS();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnCameraIdleListener(mCameraListener);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_logout){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        mAuthProvider.logout();
        Intent intent = new Intent(MapClientActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    void generateToken(){
        mTokenProvider.create(mAuthProvider.getId());
    }

    private double getDistanceBetween(LatLng clientLatLng, LatLng driverLatLng){
        double distance = 0 ;
        Location clientLocation = new Location("");
        Location driverLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driverLocation.setLatitude(driverLatLng.latitude);
        driverLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driverLocation);
        timeToHome = "" + clientLocation.getTime();
        return distance;
    }

    private void sendNotificationsPositions(){

    }


    private void sendNotification(final String distanceToHome, final String timeToHome) {
        mTokenProvider.getToken(mAuthProvider.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String ,String > map = new HashMap<>();
                    map.put("title", "Nuestro camión recolector esta por llegar!");
                    map.put("body", "Nuestro camión se encuentra a una distacia de "+ distanceToHome +" metros de tu posición actual \n Estimamos que llegue en " + timeToHome + " minutos a tu domicilio \n Preparate =)");
                    FCMBody fcmBody = new FCMBody(token,"high",map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1 ){ //puede dar un error pilas en el getSuccess
                                    //Toast.makeText(MapClientActivity.this, "Se envio la notificación", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(MapClientActivity.this, "No se envio tu fritada", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(MapClientActivity.this, "No se envio tu fritada", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", "Error: " + t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(MapClientActivity.this, "No se existe token de cliente", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //mNotificationProvider.sendNotification()
    }




}
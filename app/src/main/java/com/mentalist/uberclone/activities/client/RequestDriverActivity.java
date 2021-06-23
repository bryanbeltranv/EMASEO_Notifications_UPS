package com.mentalist.uberclone.activities.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mentalist.uberclone.R;
import com.mentalist.uberclone.models.ClientBooking;
import com.mentalist.uberclone.models.FCMBody;
import com.mentalist.uberclone.models.FCMResponse;
import com.mentalist.uberclone.providers.AuthProvider;
import com.mentalist.uberclone.providers.ClientBookingProvider;
import com.mentalist.uberclone.providers.GeoFireProvider;
import com.mentalist.uberclone.providers.GoogleApiProvider;
import com.mentalist.uberclone.providers.NotificationProvider;
import com.mentalist.uberclone.providers.TokenProvider;
import com.mentalist.uberclone.utils.DecodePoints;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;
    private GeoFireProvider mGeoFireProvider;

    private String mExtraOrigin;
    private String mExtraDestination;
    private double mExtraDestinationLat;
    private double mExtraDestinationLng;
    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;

    private GoogleApiProvider mGoogleApiProvider;


    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;
    private TokenProvider mTokenprovider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;


    private NotificationProvider mNotificationProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);

        mAnimation = findViewById(R.id.animation);
        mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
        mButtonCancelRequest = findViewById(R.id.btnCancelRequest);

        mAnimation.playAnimation();

        mExtraOrigin = getIntent().getStringExtra("origin");
        mExtraDestination = getIntent().getStringExtra("destination");

        mExtraDestinationLat = getIntent().getDoubleExtra("destination_lat", 0);
        mExtraDestinationLng = getIntent().getDoubleExtra("destination_lng", 0);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
        mOriginLatLng = new LatLng(mExtraOriginLat,mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinationLat,mExtraDestinationLng);

        mGeoFireProvider = new GeoFireProvider();
        mTokenprovider = new TokenProvider();
        mNotificationProvider = new NotificationProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);
        getClosetsDriver();


    }

    private void getClosetsDriver(){
        mGeoFireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!mDriverFound){
                    mDriverFound = true;
                    mIdDriverFound = key;
                    mDriverFoundLatLng = new LatLng(location.latitude,location.longitude);
                    mTextViewLookingFor.setText("CONDUCTOR ENCONTRADO .. ESPERANDO RESPUESTA");
                    createClientBooking();
                    Log.d("DRIVER","ID" + mIdDriverFound);
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                //INGRESA CUANDO TERMINA LA BUSQUEDA EN UN RADIO DE 0.1 KM
                if(!mDriverFound){
                    mRadius = mRadius + 0.1f;

                    //NO ENCONTRO NINGUN CERCANO
                    if(mRadius > 5){
                        mTextViewLookingFor.setText("NO SE ENCONTRO CONDUCTOR");
                        Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO CONDUCTOR", Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        getClosetsDriver();
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String time, final String km) {
        mTokenprovider.getToken(mIdDriverFound).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String token = snapshot.child("token").getValue().toString();
                    Map<String ,String > map = new HashMap<>();
                    map.put("title", "SOLICITUD DE SERVICIO A " + time + " DE TU POSICIÓN");
                    map.put("body",
                            "UN CLIENTE ESTA ENVIANDO UN SERVICIO A UNA DISTANCIA DE: " + km + "\n" + "Recoger en: " + mExtraOrigin + "\n" + "Destino " + mExtraDestination);
                    map.put("idClient", mAuthProvider.getId());
                    FCMBody fcmBody = new FCMBody(token,"high",map);
                    mNotificationProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if(response.body() != null){
                                if(response.body().getSuccess() == 1 ){ //puede dar un error pilas en el getSuccess
                                   // Toast.makeText(RequestDriverActivity.this, "Se envio la notificación", Toast.LENGTH_SHORT).show();
                                    ClientBooking clientBooking = new ClientBooking(
                                            mAuthProvider.getId(),
                                            mIdDriverFound,
                                            mExtraDestination,
                                            mExtraOrigin,
                                            time,
                                            km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );
                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RequestDriverActivity.this, "La peticion se inserto correctamente", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(RequestDriverActivity.this, "No se envio tu fritada", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(RequestDriverActivity.this, "No se envio tu fritada", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("ERROR", "Error: " + t.getMessage());
                        }
                    });
                }else{
                    Toast.makeText(RequestDriverActivity.this, "No se existe token de conductor", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //mNotificationProvider.sendNotification()
    }

    private void createClientBooking(){

        mGoogleApiProvider.getDirections(mOriginLatLng, mDriverFoundLatLng).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try{
                    JSONObject jsonObject =  new JSONObject(response.body());
                    JSONArray jsonArray =  jsonObject.getJSONArray("routes");
                    JSONObject route = jsonArray.getJSONObject(0);
                    JSONObject polylines = route.getJSONObject("overview_polyline");
                    String points = polylines.getString("points");

                    JSONArray legs = route.getJSONArray("legs");
                    JSONObject leg = legs.getJSONObject(0);

                    JSONObject distance = leg.getJSONObject("distance");
                    JSONObject duration = leg.getJSONObject("duration"); //reemplazar con duration_in_traffic con trafico

                    String distanceText = distance.getString("text");
                    String durationText = duration.getString("text");

                    sendNotification(durationText, distanceText);

                }catch (Exception e){
                    Log.d("ERROR", "Error encontrado" + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }
}
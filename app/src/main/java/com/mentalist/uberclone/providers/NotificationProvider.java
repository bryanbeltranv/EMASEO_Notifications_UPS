package com.mentalist.uberclone.providers;


import com.mentalist.uberclone.models.FCMBody;
import com.mentalist.uberclone.models.FCMResponse;
import com.mentalist.uberclone.retrofit.IFCMApi;
import com.mentalist.uberclone.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {
    private String url = "https://fcm.googleapis.com";
    public NotificationProvider(){

    }
    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClientObject(url).create(IFCMApi.class).send(body);
    }
}

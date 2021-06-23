package com.mentalist.uberclone.retrofit;

import com.mentalist.uberclone.models.FCMBody;
import com.mentalist.uberclone.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAOi_L5Cs:APA91bGDYkr51Q2j7izg3YjVU-XGBiWIgryqX4homuFKjOmI9GQCKOStFPG-TXhgu41xvR_Ezc7mm2PTny6yyb7rcqXv4oOWXu5fmHe63OhpkGY5uX7mK8mvv5K-IDOJ7pxI-nhAETXj"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}

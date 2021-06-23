package com.mentalist.uberclone.channel;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.mentalist.uberclone.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.mentalist.uberclone";
    private static final String CHANNEL_NAME = "UBER_CLONE";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannels();
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private  void createChannels(){
        NotificationChannel notificacionChannel = new
                NotificationChannel(
                        CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificacionChannel.enableLights(true);
        notificacionChannel.enableVibration(true);
        notificacionChannel.setLightColor(Color.GRAY);
        notificacionChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificacionChannel);

    }

    public NotificationManager getManager(){
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    //CONFIGURACION DE LA NOTIFICACIÓN
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificacion(String title, String body, PendingIntent intent, Uri soundUri){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_trash)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotificacionActions(String title,
                                                       String body,
                                                       Uri soundUri,
                                                       Notification.Action acceptation,
                                                       Notification.Action cancelAction){
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(acceptation)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_trash)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificacionOldAPI(String title, String body, PendingIntent intent, Uri soundUri){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.ic_trash)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

    public NotificationCompat.Builder getNotificacionOldAPIActions(String title,
                                                                   String body,
                                                                   Uri soundUri,
                                                                   NotificationCompat.Action acceptacion,
                                                                   NotificationCompat.Action cancelAction){
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(soundUri)
                .addAction(acceptacion)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_trash)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body).setBigContentTitle(title));
    }

}

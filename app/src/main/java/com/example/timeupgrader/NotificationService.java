package com.example.timeupgrader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends Service {

    private TaskDatabaseHelper dbHelper;
    private FireBaseHelper fbHelper;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new TaskDatabaseHelper(this);
        fbHelper = new FireBaseHelper();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        int alarmCount = intent.getIntExtra("alarmCount", 0);
        String name = intent.getStringExtra("actName");
        String id = intent.getStringExtra("actId");
        Log.i("alarmCount", alarmCount + "");
        Log.i("name", name);
        Log.i("id", id);
        if (alarmCount != 0 && !name.equals("") && !id.equals("")) {
            Intent newIntent = new Intent(NotificationService.this, MainActivity.class);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent contentIntent = PendingIntent.getActivity(this, alarmCount, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notify");
            Notification notification = builder.setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Time for activity!")
                    .setContentText("Your activity " + name + " has started.")
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setLights(Color.GREEN, 1000, 1000)
                    .setVibrate(new long[] { 0, 700, 700, 700 })
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("notify", "Time for activity!", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Your activity " + name + " has started.");
                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[] { 0, 700, 700, 700 });
                channel.setBypassDnd(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(alarmCount, notification);
            dbHelper.updateActivityStatus(id, SingleAct.START);
            fbHelper.updateActStatusById(id, SingleAct.START);
        }
        return START_REDELIVER_INTENT;
    }
}

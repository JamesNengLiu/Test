package com.example.outpop;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;


public class NotificationConnection implements ServiceConnection {
    private final static String TAG = "NotificationConnection";

    public static void startNotificationService(Context ctx) {
        try {
            //开启常驻通知栏
            Intent intent = new Intent("com.qhll.weather.action.PERSISTENT_NOTIFICATION");
            intent.setPackage(ctx.getPackageName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent);
            } else {
                ctx.startService(intent);
            }
            ctx.bindService(intent, NotificationConnection.getInstance(), Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NotificationConnection getInstance() {
        return Singleton.NOTIFICATION_CONNECTION;
    }

    private NotificationConnection() {

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
    }


    static class Singleton {
        final static NotificationConnection NOTIFICATION_CONNECTION = new NotificationConnection();
    }
}

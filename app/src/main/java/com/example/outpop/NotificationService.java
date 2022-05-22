package com.example.outpop;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;


import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


public class NotificationService extends Service {
    private final static String CLASS_NAME = NotificationService.class.getName();
    private final static int NOTIFICATION_UPDATE_MESSAGE = 0x12;
    private RemoteViews notificationView;
    private Notification notification;
    public final static int ID_FOR_CUSTOM_VIEW = CLASS_NAME.hashCode();
    private static final String TAG = "NotificationService";
    private NotificationManager mNotificationManager;
    private UpdateHandler mH;


    public NotificationService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        onStartCommand(intent,0,0);
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mH = new UpdateHandler(this);
        initNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        //startForeground需要放在这里更新，如果放在onCreate如果Service被系统重启是不会再走onCreate，导致异常
        try {
            if (notification != null) {
                startForeground(ID_FOR_CUSTOM_VIEW, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "onStartCommand: ", e);
            // 系统可能抛出空指针异常，捕获后停止服务
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }
        final Bundle data = intent.getExtras();
        mH.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateNotificationView(data);
            }
        }, 100);
        //这里不用START_STICKY，可能会导致binder cast 失败的问题，https://stackoverflow.com/questions/14458627/classcastexception-android-os-binderproxy-cannot-be-cast
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        stop();
    }

    private void initNotification() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, createNotificationChannel(this));
        builder.setSmallIcon(R.drawable.notiflogo)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MAX)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
            updateNotificationView();
            builder.setCustomContentView(notificationView);
            notification = builder.build();
    }

    public void stop() {
        try {
            mH.removeCallbacksAndMessages(null);
            mNotificationManager.cancel(ID_FOR_CUSTOM_VIEW);
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        } catch (Exception e) {
            Log.i(TAG, "set service for push exception: ", e);
        }
    }

    public void updateNotificationView() {
            if (notificationView == null) {
                notificationView = new RemoteViews(getPackageName(), getNotificationLayout());
            }
    }

    private int getNotificationLayout() {

                return R.layout.notification_persistance;
    }

    public void updateNotificationView(Bundle data) {

        updateNotificationView();

        if (data == null) {
            return;
        }

    }

    //9.0通知栏  适配
    public static String createNotificationChannel(Context context) {

        String channelId = "channelId";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = context.getApplicationInfo().loadLabel(context.getPackageManager());
            String channelDescription = "天气情况实时通知";
            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, channelImportance);
            // 设置描述 最长30字符
            notificationChannel.setDescription(channelDescription);
            // 该渠道的通知是否使用震动
            notificationChannel.enableVibration(false);
            // 设置显示模式
            notificationChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

        }
        return channelId;
    }

    static class UpdateHandler extends Handler {

        private final Reference<NotificationService> serviceRef;

        UpdateHandler(NotificationService service) {
            serviceRef = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NOTIFICATION_UPDATE_MESSAGE:
                    if (serviceRef.get() != null) {
                        serviceRef.get().updateNotificationView(msg.getData());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}

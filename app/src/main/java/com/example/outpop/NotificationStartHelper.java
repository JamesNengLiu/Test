package com.example.outpop;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.qhll.oppotools.OppoBackgroundLimitRemove;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 先用  PendingIntent.send() 启动 Activity ，如果有异常，则调用 startActivity() 的来启动。
 * <p>
 * 然后，再创建一个 NotificationChannel，然后发出这个广播即可。
 * <p>
 * 分析，应该是MIUI的com.android.server.am.ExtraActivityManagerService 代码中，
 * <p>
 * 判断了要启动的 Activity ，如果在通知栏中的PendingIntent 中存在的话，则不进行拦截。
 */
public class NotificationStartHelper {
    public static final String TAG = "NotificationStartHelper";
    private static final String CHANNEL_ID = "sm_lkr_ntf_hl_pr_chn_id_7355608_wtf";
    private static final int REQ_CODE = 10102;
    private static final int FAKE_NOTIFICATION_ID = 10101;
    private static final String FAKE_NOTIFICATION_TAG = "AA_TAG1";

    private static final String FINGERPRINT = Build.FINGERPRINT == null ? "" : Build.FINGERPRINT.toLowerCase();

    private static final String BRAND = Build.BRAND == null ? "" : Build.BRAND.toLowerCase();

    private static final String MANUFACTURER = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase();

    private static Handler sHandler;

    public static Set<String> START_FLAG = new HashSet<>();

    private static boolean isMiuiRom() {
        return BRAND.contains("xiaomi") || MANUFACTURER.contains("xiaomi") || FINGERPRINT.contains("miui") || FINGERPRINT.contains("xiaomi");
    }

    public static boolean isHuawei() {
        return BRAND.contains("huawei") || MANUFACTURER.contains("huawei");
    }

    public static boolean isHonor() {
        return BRAND.contains("honor") || MANUFACTURER.contains("honor") || BRAND.contains("荣耀") || MANUFACTURER.contains("荣耀");
    }

    public static boolean isOppoRom() {
        return BRAND.contains("oppo") || MANUFACTURER.contains("oppo");
    }

    public static boolean isVivoRom() {
        return BRAND.contains("vivo") || MANUFACTURER.contains("vivo");
    }

    public static boolean isSupport() {
        if (isMiuiRom()) {
            return Build.VERSION.SDK_INT >= 25 && Build.VERSION.SDK_INT < 29;
        } else if (isHuawei()) {
            return (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT <= 25);
        } else if (isOppoRom()) {
            return Build.VERSION.SDK_INT >= 25 && Build.VERSION.SDK_INT < 29;
        } else if (isVivoRom()) {
            return Build.VERSION.SDK_INT >= 25 && Build.VERSION.SDK_INT < 29;
        } else if (isHonor()) {
            return Build.VERSION.SDK_INT >= 28;
        } else {
            return false;
        }
    }

    public static final String PLUGIN_NAME_CLEAN = "com.qhll.cleanmaster.plugin.clean";

    public static boolean startActivityByPendingIntent(Context context, Intent intent, String eventId) {
        return startActivityByPendingIntent(context, intent, eventId, false);
    }

    public static boolean startActivityByPendingIntent(Context context, Intent intent, String eventId, boolean useCustomContainer) {
        boolean start = false;
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return false;
        }
        final String activityClassName = componentName.getClassName();
        START_FLAG.add(activityClassName);
        if (isSupport()) {
            startActivity(context, intent, 0x7f060000);
            start = true;
        } else if (Build.VERSION.SDK_INT >= 23) {
            startByAlarmManager(context, intent);
            start = true;
        }
        if (start) {
            if (sHandler == null) {
                sHandler = new Handler(Looper.getMainLooper());
            }
            Intent finalIntent = intent;
            sHandler.postDelayed(() -> {
                if (START_FLAG.contains(activityClassName)) {
                    Log.d(TAG, "startActivityByPendingIntent: delay " +activityClassName);
                    finalIntent.putExtra("start_way", "restart");
                    finalIntent.setComponent(componentName);
                    try {
                        context.startActivity(finalIntent);
                    } catch (Exception e) {
                        Map<String, String> attrs = new HashMap<>(4);
                        if (finalIntent.getComponent() != null) {
                            attrs.put("name", "1_" + finalIntent.getComponent().getShortClassName());
                        }
                        attrs.put("ex", "1_" + e.getMessage());
                    }
                }
            }, 2000);
        } else {
            intent.setComponent(componentName);
            context.startActivity(intent);
        }
        return start;
    }

    public static boolean startActivityByPendingIntent(Context context, Intent intent) {
        return startActivityByPendingIntent(context, intent, null);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean startByAlarmManager(Context context, Intent intent) {
        intent.putExtra("start_way", "AlarmManager");
        Log.e(TAG, "使用AlarmManager方式");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // 不需要后台唤醒
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 200, pendingIntent);
            return true;
        }
        return false;
    }

    private static void startActivity(Context paramContext, Intent intent, int icon) {
        intent.putExtra("start_way", "notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(paramContext, REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        sendToNotification(paramContext, pendingIntent, icon);

        safelyStartActivity(paramContext, pendingIntent, intent);
    }

    private static void sendToNotification(Context paramContext, PendingIntent paramPendingIntent, int icon) {
        NotificationManager notificationManager = (NotificationManager) paramContext.getSystemService(Context.NOTIFICATION_SERVICE);
        initNotificationChannel(notificationManager);

        Notification.Builder builder = null;
//        // O above ,use channelId
        if (Build.VERSION.SDK_INT >= 26) {
            try {
                Constructor constructor = Notification.Builder.class.getDeclaredConstructor(Context.class, String.class);
                builder = (Notification.Builder) constructor.newInstance(paramContext, CHANNEL_ID);
            } catch (Exception e) {
                //quiet
            }
            //异常的话，则使用旧版本的方式来创建 Builder 对象
            if (builder == null) {
                builder = new Notification.Builder(paramContext);
            }
        } else {
            builder = new Notification.Builder(paramContext);
        }

        builder.setSmallIcon(icon);
        builder.setFullScreenIntent(paramPendingIntent, true);
        builder.setAutoCancel(true);

        notificationManager.cancel(FAKE_NOTIFICATION_TAG, FAKE_NOTIFICATION_ID);
        notificationManager.notify(FAKE_NOTIFICATION_TAG, FAKE_NOTIFICATION_ID, builder.getNotification());

        // 1秒后cancel 掉该 notification
        delayCancelNotification(paramContext);
    }

    private static void safelyStartActivity(Context paramContext, PendingIntent pendingIntent, Intent intent) {
        try {
            pendingIntent.send();
        } catch (Throwable throwable) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY);
                paramContext.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }


    private static void initNotificationChannel(NotificationManager paramNotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && paramNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "天气不好", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("天气预报");
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setShowBadge(false);
            notificationChannel.setSound(null, null);
            notificationChannel.setBypassDnd(true);
            paramNotificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private static void delayCancelNotification(final Context context) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissNotification(context);
            }
        }, 1000);
    }

    private static void dismissNotification(Context context) {
        if (context != null) {
            try {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(FAKE_NOTIFICATION_TAG, FAKE_NOTIFICATION_ID);
            } catch (Throwable ignored) {
            }
        }
    }
}

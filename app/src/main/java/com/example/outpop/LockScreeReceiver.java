package com.example.outpop;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LockScreeReceiver extends BroadcastReceiver {

    private static final String TAG = "LockScreeReceiver";

    private static final boolean sIsOPPO;

    private Context mCtx;

    static {
        if (Build.BRAND != null) {
            sIsOPPO = Build.BRAND.toLowerCase().contains("oppo");
        } else {
            sIsOPPO = false;
        }
    }

    private Handler mMainHandler = new LockScreenHandler(Looper.getMainLooper());

    @Nullable
    private ScheduledExecutorService mScheduledExecutorService = null;

    public LockScreeReceiver(Context context) {
        mCtx = context;
        if (sIsOPPO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                mScheduledExecutorService = Executors.newScheduledThreadPool(1);
                mScheduledExecutorService.scheduleWithFixedDelay(
                        new PollWorker((PowerManager) context.getSystemService(Context.POWER_SERVICE)),
                        1L, 1L, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        Log.d("lxtest","onReceive "+intent);
        Intent intent1 = new Intent();
        intent1.setClassName(context.getPackageName(),MainActivity2.class.getName());
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        switch (intent.getAction()) {
            case Intent.ACTION_SCREEN_OFF:
                Message msg = Message.obtain(mMainHandler, LockScreenHandler.MSG_SCREEN_OFF);
                msg.obj = intent;
                mMainHandler.sendMessage(msg);
                break;
            case Intent.ACTION_SCREEN_ON:
                msg = Message.obtain(mMainHandler, LockScreenHandler.MSG_SCREEN_ON);
                msg.obj = intent;
                mMainHandler.sendMessage(msg);
                break;
            case Intent.ACTION_USER_PRESENT:
                handleUserPresend(intent);
                NotificationStartHelper.startActivityByPendingIntent(context,intent1);
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                handleWifiStateChangeAction(intent);
                break;
            case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                handleWifiNetworkStateChangeAction(intent);
                break;
            case Intent.ACTION_POWER_CONNECTED:
                handleActionPowerConnected(intent);
                break;
            case Intent.ACTION_POWER_DISCONNECTED:
                handleActionPowerDisconnected(intent);
                break;
            case "android.net.conn.CONNECTIVITY_CHANGE":
                handleConnectivityChange(intent);
                break;
            case TelephonyManager.ACTION_PHONE_STATE_CHANGED:
                handlePhoneStateChanged(intent);
                break;
            default:
        }
    }

    private void handleScreenOff(Intent intent, boolean fromPollWork) {
    }

    private void handleScreenOn(Intent intent, boolean fromPollWork) {
//        QLockScreenProxy.forwardReceiver(intent);
        Log.d(TAG, "handleScreenOn: ");
        Intent intent1 = new Intent();
        intent1.setClassName(mCtx.getPackageName(),MainActivity2.class.getName());
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        NotificationStartHelper.startActivityByPendingIntent(mCtx,intent1);
    }

    private void handleUserPresend(Intent intent) {
    }

    private void handleWifiStateChangeAction(Intent intent) {
    }

    private void handleWifiNetworkStateChangeAction(Intent intent) {
    }

    private void handleActionPowerConnected(Intent intent) {
    }

    private void handleActionPowerDisconnected(Intent intent) {
    }

    private void handleConnectivityChange(Intent intent) {
    }

    private void handlePhoneStateChanged(Intent intent) {
//        QLockScreenProxy.forwardReceiver(intent);
    }

    private void destroyPollWork() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        destroyPollWork();
        super.finalize();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private class PollWorker implements Runnable {

        private PowerManager mPowerManager;

        /**
         * 这里设置为 true，这样在初始化的时候（通常为亮屏状态）不会发送 on 事件
         */
        private boolean mLastInteractiveState = true;

        public PollWorker(PowerManager powerManager) {
            this.mPowerManager = powerManager;
        }

        @Override
        public void run() {
            if (mPowerManager == null || mMainHandler == null) {
                return;
            }
            Log.d(TAG, "PollWorker::run: "+mLastInteractiveState);
            boolean b = mPowerManager.isInteractive();
            if (mLastInteractiveState != b) {
                mLastInteractiveState = b;
                Message msg = Message.obtain(mMainHandler, b ? LockScreenHandler.MSG_SCREEN_ON : LockScreenHandler.MSG_SCREEN_OFF);
                msg.arg1 = 1;
                mMainHandler.sendMessage(msg);
            }
        }
    }

    private class LockScreenHandler extends Handler {

        static final int MSG_SCREEN_ON = 0X0001;

        static final int MSG_SCREEN_OFF = 0X0002;

        private Boolean mIsScreenOn = null;

        LockScreenHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCREEN_ON:
                    if (mIsScreenOn != null && mIsScreenOn) {
                        // 亮屏事件已经发送过了，不重复转发处理
                        break;
                    }
                    mIsScreenOn = true;
                    handleScreenOn(msg.obj == null ? new Intent(Intent.ACTION_SCREEN_ON) : ((Intent) msg.obj), msg.arg1 != 0);
                    break;
                case MSG_SCREEN_OFF:
                    if (mIsScreenOn != null && !mIsScreenOn) {
                        // 熄屏事件已经发送过了，不重复转发处理
                        break;
                    }
                    mIsScreenOn = false;
                    handleScreenOff(msg.obj == null ? new Intent(Intent.ACTION_SCREEN_OFF) : ((Intent) msg.obj), msg.arg1 != 0);
                    break;
                default:
            }
        }
    }
}

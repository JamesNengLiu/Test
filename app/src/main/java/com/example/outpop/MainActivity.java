package com.example.outpop;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.outpop.databinding.ActivityMainBinding;
import com.qhll.oppotools.OppoBackgroundLimitRemove;
import com.qhll.oppotools.OppoToolsManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LockScreeReceiver mLockScreeReceiver = new LockScreeReceiver(this.getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        this.getApplicationContext().registerReceiver(mLockScreeReceiver, filter);

//        initHookHost(getApplicationContext());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationConnection.startNotificationService(getApplicationContext());

            }
        },2000);

        binding.btn.setOnClickListener((view)->{
//            Intent intent =new Intent();
////            intent.setClassName("com.weather.litchi","com.qhll.cleanmaster.ui.MemClearActivity");
//            intent.setClassName("com.weather.litchi","com.qhll.cleanmaster.MainActivity");
//            startActivity(intent);

            openApp("com.weather.litchi");
        });


        PackageInfo pi = null;

        try {
            pi = getPackageManager().getPackageInfo("com.example.outpop", 0);
            Log.d("lxtest","pi "+pi);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("lxtest","MainActvity.onCreate");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("lxetest","MainActi.Destr");
    }

    private void openApp(String packageName) {
        final PackageManager packageManager = getPackageManager();
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(pi == null || pi.packageName == null){
            Toast.makeText(this, "还没安装！", 2000).show();
            return;
        }
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);

        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);

        ResolveInfo ri = apps.iterator().next();
        if (ri != null) {
            String pkgName = ri.activityInfo.packageName;
            String className = ri.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            ComponentName cn = new ComponentName(pkgName, className);

            intent.setComponent(cn);
            startActivity(intent);
        }
    }

    private void initHookHost(Context context) {
        OppoToolsManager.getInstance().hookStartActivity(new OppoToolsActivityListener());
    }

    private static class OppoToolsActivityListener implements OppoToolsManager.StartActivityListener {

        private static final String PREFIX_ACTIVITY = "activity:";


        @Override
        public void onStartActivity(Intent intent) {
            Set<String> categories = intent.getCategories();
            String realIntentActivity = null;
//            if (categories != null) {
//                boolean found = false;
//                for (String s : categories) {
//                    if (s.startsWith(PREFIX_ACTIVITY)) {
//                        realIntentActivity = s.replace(PREFIX_ACTIVITY, "");
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found && intent.getComponent() != null) {
                    realIntentActivity = intent.getComponent().getClassName();
//                }
//            }
            if (!TextUtils.isEmpty(realIntentActivity)) {
                    Log.i("OTAL", String.format("Activity [%s] matched!", realIntentActivity));
                try {
                    // 播放音频，增强弹出
                    OppoBackgroundLimitRemove.getInstance().disableOppoBackgroundLimitTemporary();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
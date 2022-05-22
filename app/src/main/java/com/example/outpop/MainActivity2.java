package com.example.outpop;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "MainActivity2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initWindow();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toast.makeText(this, "main2", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: start_way "+getIntent().getStringExtra("start_way"));
    }

    private void initWindow() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final Window win = getWindow();
        final WindowManager.LayoutParams params = win.getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(params);
        // 1. 通知栏背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // 5.0及以上
            getWindow().setStatusBarColor(Color.parseColor("#000000"));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    // 4.4到5.0
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup contentView = findViewById(android.R.id.content);
            View statusBarView = new View(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight(this));
            statusBarView.setBackgroundColor(Color.parseColor("#000000"));
            contentView.addView(statusBarView, lp);
        }
    }

    //得到系统statusbar的高度
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
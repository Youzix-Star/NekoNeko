package com.youzix.nekoneko;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

/**
 * 应用入口：在 Android 12+ 上为所有 Activity 应用莫奈（Material You）动态取色。
 * 低版本系统自动回退到主题中的浅蓝色系配色。
 */
public class NekoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}

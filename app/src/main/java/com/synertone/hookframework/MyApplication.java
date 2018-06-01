package com.synertone.hookframework;

import android.app.Application;

import com.synertone.hookframework.utils.HookUtil;

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtil hookUtil=HookUtil.getInstance(this);
        hookUtil.hookStartActivity();
        hookUtil.hookActivityThreadmH();
    }
}

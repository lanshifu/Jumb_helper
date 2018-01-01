package com.lanshifu.jump_helper;

import android.app.Application;
import android.content.Context;

import com.lanshifu.jump_helper.utils.ToastUtil;

/**
 * Created by lanshifu on 2017/12/10.
 */

public class MainApplication extends Application {

    public static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        ToastUtil.init(getApplicationContext());
    }

    public static Context getContext(){
        return mContext;
    }
}

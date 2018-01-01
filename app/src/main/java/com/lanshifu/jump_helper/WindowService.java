package com.lanshifu.jump_helper;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;
import com.lanshifu.jump_helper.utils.LogUtil;
import com.lanshifu.jump_helper.utils.SPUtil;
import com.lanshifu.jump_helper.utils.ToastUtil;

/**
 * Created by lWX385269 lanshifu on 2017/2/8.
 */

public class WindowService extends Service {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private View mWindowView;
    private TextView mTvScreen;
    private int mStartX;
    private int mStartY;
    private int mEndX;
    private int mEndY;
    private TextView mTvAppend;
    private boolean mIsPre;

    private int mPressTime = 0;
    private float number = 2;

    private static final int WHAT_SHOW = 0;
    private static final int WHAT_HIDE = 1;
    private static final int WHAT_JUMP = 2;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_SHOW:
                    show();
                    break;
                case WHAT_HIDE:
                    hide();
                    break;
                case WHAT_JUMP:
                    jump();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initWindowParams();
        initView();
        addWindowView();
        String string = SPUtil.getInstance().getString(SPUtil.NUMBER, "0.75");
        number = Float.valueOf(string);

    }

    private void addWindowView() {
        mWindowManager.addView(mWindowView, wmParams);
    }

    private void initView() {
        mWindowView = LayoutInflater.from(this).inflate(R.layout.layout_windw, null);
        mTvScreen = (TextView) mWindowView.findViewById(R.id.tv_srceen);
        mTvAppend = (TextView) mWindowView.findViewById(R.id.tv_append);
        mTvAppend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTvScreen.getVisibility() == View.VISIBLE) {
                    hide();
                } else {
                    show();
                }
            }
        });


    }

    private void initWindowParams() {
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
//        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.TRANSLUCENT;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWindowView != null) {
            mWindowManager.removeView(mWindowView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    View.OnTouchListener onTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mIsPre) {
                        mIsPre = false;
                        mStartX = (int) event.getRawX();
                        mStartY = (int) event.getRawY();
                        ToastUtil.showShort("起点 ：" + mStartX + "," + mStartY);
                        LogUtil.d("起点 ：" + mStartX + "," + mStartY);
                        return true;
                    }
                    mEndX = (int) event.getRawX();
                    mEndY = (int) event.getRawY();
                    ToastUtil.showShort("终点 ：" + mEndX + "," + mEndY);
                    LogUtil.d("终点 ：" + mEndX + "," + mEndY);
                    //计算时间
                    int dist = calDistance();
                    mPressTime = (int) (dist / number);
                    LogUtil.d("系数 = " + number + ",距离=" + dist + " 时间=" + mPressTime);
                    mIsPre = true;
                    hide();
                    mHandler.sendEmptyMessageDelayed(WHAT_JUMP, 100);
                    break;

            }
            return false;
        }
    };


    private void jump() {
        new Thread() {
            @Override
            public void run() {

                String commands = "input touchscreen swipe 170 170 170 170 " + mPressTime;
                CommandResult commandResult = Shell.SU.run(commands);
                String stdout = commandResult.getStdout();
                LogUtil.d("getStdout " + stdout);
                if (mIsPre) {
                    mHandler.sendEmptyMessageDelayed(WHAT_SHOW, 100);
                }
            }
        }.start();
    }

    private void hide() {
        mTvScreen.setVisibility(View.GONE);
        mTvAppend.setText("开始");
        mTvScreen.setOnTouchListener(null);
        mIsPre = true;
    }

    private void show() {
        mTvScreen.setVisibility(View.VISIBLE);
        mTvAppend.setText("暂停");
        mTvScreen.setOnTouchListener(onTouchListener);
        mIsPre = true;
    }


    public int calDistance() {
        return (int) Math.sqrt((mStartX - mEndX) * (mStartX - mEndX) + (mStartY - mEndY) * (mStartY - mEndY));
    }
}

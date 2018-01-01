package com.lanshifu.jump_helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.jaredrummler.android.shell.Shell;
import com.lanshifu.jump_helper.utils.LogUtil;
import com.lanshifu.jump_helper.utils.SPUtil;
import com.lanshifu.jump_helper.utils.ToastUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetCallback;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class MainActivity extends AppCompatActivity {

    private Intent mService;
    private EditText mEtNumber;
    private Button mBtnStart;
    private Button mBtn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEtNumber = (EditText) findViewById(R.id.et_number);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtn_save = (Button) findViewById(R.id.btn_ok);
        String number = SPUtil.getInstance().getString(SPUtil.NUMBER, "0.75");
        mEtNumber.setText(number);

        checkRootPermission();

    }

    private void checkRootPermission() {
        Bmob.initialize(this, "8697275d4a9de20e56f88c13e2720993");
        new Thread(){
            @Override
            public void run() {
                boolean permission = Shell.SU.available();
                if (!permission){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShort("没有root权限，无法使用");
                            mBtnStart.setEnabled(false);
                            mBtn_save.setEnabled(false);
                        }
                    });
                }else {
                    requestNet();
                }
            }
        }.start();
    }

    private void requestNet() {
        BmobQuery<JumpHelperConfig> bmobQuery = new BmobQuery<JumpHelperConfig>();
        bmobQuery.getObject(this, "f5604447", new GetListener<JumpHelperConfig>() {
            @Override
            public void onSuccess(JumpHelperConfig jumpHelperConfig) {
                Boolean enable = jumpHelperConfig.getEnable();
                if (!enable){
                    String message = jumpHelperConfig.getMessage();
                    ToastUtil.showShort(message);
                    finish();
                }else {
                    query();
                }
            }

            @Override
            public void onFailure(int i, String s) {
                LogUtil.e(s);
            }
        });

    }

    private void query(){
        BmobQuery<JumpHelperData> query = new BmobQuery<JumpHelperData>();
        query.addWhereEqualTo("name",getSerialNumber());
        query.findObjects(this, new FindListener<JumpHelperData>() {
            @Override
            public void onSuccess(List<JumpHelperData> list) {
                if (list.size() == 0){
                    insertData();
                }else {
                    JumpHelperData data = list.get(0);
                    if(!data.getEnable()){
                        ToastUtil.showShort(data.getMessage());
                        finish();
                    }

                    data.setCount(data.getCount().intValue() + 1);
                    data.update(MainActivity.this);
                }
            }

            @Override
            public void onError(int i, String s) {

            }
        });
    }

    private void insertData() {
        String number = getSerialNumber();
        JumpHelperData data = new JumpHelperData();
        data.setName(number);
        data.setCount(1);
        data.setEnable(true);
        data.setModel(android.os.Build.MODEL);
        data.save(this, new SaveListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i, String s) {
                LogUtil.e(s);
            }
        });

    }

    private static final int REQUEST_CODE = 1;

    private void requestAlertWindowPermission() {
        mService = new Intent(this,WindowService.class);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this)) {
                startService(mService);
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {
            startService(mService);
        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            LogUtil.d("有权限");
        }
    }

    public void start(View view) {
        requestAlertWindowPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            stopService(mService);
        }
    }

    public void Save(View view) {
        if (TextUtils.isEmpty(mEtNumber.getText())){
            ToastUtil.showShort("不能为空");
            return;
        }
        SPUtil.getInstance().putString(SPUtil.NUMBER,mEtNumber.getText().toString());
        ToastUtil.showShort("保存成功，已生效");
        if (mService != null){
            stopService(mService);
            startService(mService);
        }
    }


    public static String getSerialNumber(){
        String serial = "null";
        try {
            Class<?> c =Class.forName("android.os.SystemProperties");
            Method get =c.getMethod("get", String.class);
            serial = (String)get.invoke(c, "ro.serialno");

        } catch (Exception e) {
            e.printStackTrace();
            return "null";
        }
        return serial;

    }
}

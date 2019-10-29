package com.leon.listen;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.leon.listen.App.TAG;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUrl, etKey, etName;
    private TextView tvInfo;
    private StringBuilder stringBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkService();
        etUrl = findViewById(R.id.et_url);
        etKey = findViewById(R.id.et_key);
        etName = findViewById(R.id.et_name);
        tvInfo = findViewById(R.id.tv_info);
        Button btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        etUrl.setText(SPUtils.getString("url"));
        etKey.setText(SPUtils.getString("key"));
        etName.setText(SPUtils.getString("name"));
        etUrl.setText("http://hq59d.cn/api/code/uzhifu");
        initBroadcast();
    }

    private void initBroadcast() {
        stringBuilder = new StringBuilder();
        IntentFilter filter = new IntentFilter("com.leon.listen.service.broadcast");
        BroadcastReceiver receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                stringBuilder.append(intent.getStringExtra("info"));
                tvInfo.setText(stringBuilder);
            }
        };
        registerReceiver(receiver, filter);
    }


    @Override
    public void onClick(View view) {
        String url = etUrl.getText().toString().trim();
        String key = etKey.getText().toString().trim();
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            showToast("请输入服务器地址");
        } else if (TextUtils.isEmpty(key)) {
            showToast("请输入签名校验");
        } else if (TextUtils.isEmpty(name)) {
            showToast("请输入昵称");
        } else if (!((url.startsWith("http://") || url.startsWith("https://")))) {
            showToast("服务器地址错误");
        } else if (!"9527".equals(key)) {
            showToast("签名校验错误");
        } else {
            SPUtils.setString(getApplicationContext(), "url", url);
            SPUtils.setString(getApplicationContext(), "key", key);
            SPUtils.setString(getApplicationContext(), "name", name);
            showToast("保存成功");
        }
    }

    private void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private void checkService() {
        if (!Utils.isNotificationListenerServiceEnabled(getApplicationContext())) {
            (new AlertDialog.Builder(this)).setTitle("提示").setMessage("请先授权后再继续操作!").setPositiveButton("前往授权", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface param1DialogInterface, int param1Int) {
                    Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    MainActivity.this.startActivityForResult(intent, 100);
                }
            }).create().show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, NotifiService.class));
            } else {
                startService(new Intent(this, NotifiService.class));
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            checkService();
        }
    }

}

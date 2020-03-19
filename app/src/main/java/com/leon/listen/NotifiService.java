package com.leon.listen;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.leon.listen.App.TAG;

@SuppressLint("OverrideAbstract")
public class NotifiService extends NotificationListenerService {

    private Handler mHandler = new Handler();
    String notificationId = "channelId";
    String notificationName = "channelName";

    @Override
    public void onCreate() {
        super.onCreate();
        sendInfo("初始化....");
        toggleNotificationListenerService();
    }

    private void sendInfo(String info) {
        Intent intent = new Intent("com.leon.listen.service.broadcast");
        intent.putExtra("info", info + "\n");
        sendBroadcast(intent);
    }

    //重新开启NotificationMonitor
    private void toggleNotificationListenerService() {
        ComponentName thisComponent = new ComponentName(this, NotifiService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void startForegroundService() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //创建NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        startForeground(1, getNotification());
    }

    @SuppressLint("WrongConstant")
    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("服务正在后台运行中...");
        builder.setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), 134217728));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service is onStartCommand" + "-----");
        startForegroundService();
        sendInfo("初始化成功,正在监听....");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.i(TAG, "Service is onNotificationPosted" + "-----");
        try {
            Bundle bundle = sbn.getNotification().extras;
            if (bundle != null) {
                Log.i(TAG, "Get Message bundle" + "-----" + bundle.toString());
                String title = bundle.getString("android.title", "");
                String text = bundle.getString("android.text", "");
                Log.i(TAG, "Get Message title" + "-----" + title);
                Log.i(TAG, "Get Message text" + "-----" + text);
                Log.i(TAG, "Get Message getPackageName" + "-----" + sbn.getPackageName());

                if (TextUtils.isEmpty(SPUtils.getString("url"))) {
                    return;
                }
                if (!"com.eg.android.AlipayGphone".equals(sbn.getPackageName())) {
                    return;
                }
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                if (!Utils.isMoneyNotification(text)) {
                    return;
                }
                String price = Utils.getMatchPrice(text);

                new Thread(getRunnable(this, price)).start();
            }
        } catch (Exception e) {
            Log.i(TAG, "Get Message Error");
        }
    }

    private Runnable getRunnable(final NotifiService service, final String money) {
        return new Runnable() {
            @Override
            public void run() {
                NotifiService.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            String time = Utils.getTime();
                            RequestParams requestParams = new RequestParams();
                            requestParams.put("name", SPUtils.getString("name"));
                            requestParams.put("money", money);
                            requestParams.put("paytime", time);

                            sendInfo(time + "：支付宝到账" + money);
                            Log.i(TAG, "params:" + requestParams.toString());

                            JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    super.onSuccess(statusCode, headers, response);
                                    String msg = "";
                                    try {
                                        Log.d(TAG, "onSuccess:" + response);
                                        msg = response.getString("message");
                                    } catch (Exception e) {
                                        msg = e.getMessage();
                                    }
                                    Toast.makeText(service.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    Log.i(TAG, "jsonHttpResponseHandler onFailure 网络错误" + "-----" + "fa:" + errorResponse);
                                    Toast.makeText(service.getApplicationContext(), "网络错误", Toast.LENGTH_LONG).show();
                                }
                            };

                            Http.post(SPUtils.getString("url"), requestParams, jsonHttpResponseHandler);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
    }
}

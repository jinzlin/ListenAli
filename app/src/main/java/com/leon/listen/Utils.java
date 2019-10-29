package com.leon.listen;

import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getTime(){
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            return simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static boolean isNotificationListenerServiceEnabled(Context context) {
        return NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.getPackageName());
    }

    public static String getMatchPrice(String content) {
        String result = null;
        Matcher matcher = Pattern.compile("\\d+\\.\\d+", Pattern.DOTALL | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE).matcher(content);
        if (matcher.matches()) {
            result = matcher.group();
        } else {
            result = "0";
        }
        return result;
    }

    public static String getPrice(String content) {
        try {
            Log.e("aa-----", content.split("成功收款")[1].split("元。")[0]);
            return content.split("成功收款")[1].split("元。")[0];
        } catch (Exception e) {
            return "-1";
        }
    }

}
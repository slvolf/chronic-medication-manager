package com.medication.manage;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * 应用入口
 * 负责初始化全局配置，如通知渠道
 */
public class MedicationApp extends Application {

    public static final String CHANNEL_ID = "medication_reminder";
    public static final String CHANNEL_NAME = "用药提醒";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * 创建用药提醒通知渠道（Android 8.0+ 必需）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("用药提醒通知，包含铃声和震动");
            channel.enableVibration(true);
            // 设置震动模式：等待200ms，震动1000ms，停顿500ms，再震动500ms
            channel.setVibrationPattern(new long[]{200, 1000, 500, 500});
            channel.setShowBadge(true);
            channel.setBypassDnd(true);  // 允许免打扰模式下响铃

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}

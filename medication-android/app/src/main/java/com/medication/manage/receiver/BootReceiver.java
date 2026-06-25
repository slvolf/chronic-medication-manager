package com.medication.manage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机广播接收器
 * 设备重启后重新调度定时任务
 * 注：实际项目中需要配合 WorkManager 重新注册所有周期性任务
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 重新调度定时任务
            // 实际项目中，WorkManager 会自动重新调度已持久化的 PeriodicWorkRequest
            // 这里预留扩展点
        }
    }
}

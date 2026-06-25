package com.medication.manage.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.medication.manage.MedicationApp;
import com.medication.manage.R;
import com.medication.manage.ui.dashboard.MainActivity;

/**
 * WorkManager 定时任务 Worker
 * 在设定的提醒时间触发通知提醒
 */
public class MedicationReminderWorker extends Worker {

    public MedicationReminderWorker(Context context, WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // 从输入数据中获取药品名称和计划时间
        Data inputData = getInputData();
        String drugName = inputData.getString("drugName");
        String scheduledTime = inputData.getString("scheduledTime");
        Long recordId = inputData.getLong("recordId", -1L);

        // 处理 null 默认值
        if (drugName == null) drugName = "药品";
        if (scheduledTime == null) scheduledTime = "";

        showNotification(drugName, scheduledTime, recordId);
        return Result.success();
    }

    /**
     * 发送通知栏提醒
     */
    private void showNotification(String drugName, String time, Long recordId) {
        Context context = getApplicationContext();

        // 点击通知打开主界面
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        // "已服药" 快捷按钮
        Intent checkInIntent = new Intent(context, com.medication.manage.receiver.MedicationAlarmReceiver.class);
        checkInIntent.setAction("ACTION_CHECK_IN");
        checkInIntent.putExtra("record_id", recordId);
        PendingIntent checkInPendingIntent = PendingIntent.getBroadcast(
                context, recordId.intValue(), checkInIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        // "稍后提醒" 快捷按钮
        Intent laterIntent = new Intent(context, com.medication.manage.receiver.MedicationAlarmReceiver.class);
        laterIntent.setAction("ACTION_REMIND_LATER");
        laterIntent.putExtra("record_id", recordId);
        PendingIntent laterPendingIntent = PendingIntent.getBroadcast(
                context, recordId.intValue() + 10000, laterIntent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                        : PendingIntent.FLAG_UPDATE_CURRENT);

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MedicationApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_plan)
                .setContentTitle("用药提醒")
                .setContentText("该服用 " + drugName + " 了 (" + time + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_plan, "已服药", checkInPendingIntent)
                .addAction(R.drawable.ic_plan, "稍后提醒", laterPendingIntent);

        // 发送通知
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ 需要动态权限，这里只尝试发送
            }
            notificationManager.notify(recordId.intValue(), builder.build());
        } catch (SecurityException e) {
            // 无通知权限，静默失败
        }
    }
}

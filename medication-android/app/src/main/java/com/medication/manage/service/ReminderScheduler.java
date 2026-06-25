package com.medication.manage.service;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * 用药提醒调度器
 * 使用 WorkManager 调度一次性提醒任务
 */
public class ReminderScheduler {

    /**
     * 调度一个用药提醒
     * @param context      上下文
     * @param recordId     用药记录ID
     * @param drugName     药品名称
     * @param scheduledTime 计划时间 (HH:mm)
     * @param delayMinutes 距离提醒的延迟分钟数
     */
    public static void scheduleReminder(Context context, long recordId,
                                        String drugName, String scheduledTime,
                                        long delayMinutes) {
        Data inputData = new Data.Builder()
                .putString("drugName", drugName)
                .putString("scheduledTime", scheduledTime)
                .putLong("recordId", recordId)
                .build();

        OneTimeWorkRequest reminderWork = new OneTimeWorkRequest.Builder(MedicationReminderWorker.class)
                .setInputData(inputData)
                .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                .addTag("medication_reminder")
                .build();

        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        "reminder_" + recordId,
                        ExistingWorkPolicy.REPLACE,
                        reminderWork);
    }

    /**
     * 取消一个提醒
     */
    public static void cancelReminder(Context context, long recordId) {
        WorkManager.getInstance(context)
                .cancelUniqueWork("reminder_" + recordId);
    }
}

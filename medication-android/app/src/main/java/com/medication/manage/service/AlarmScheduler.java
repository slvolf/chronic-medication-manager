package com.medication.manage.service;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.medication.manage.receiver.MedicationAlarmReceiver;

import java.util.Calendar;
import java.util.List;

/**
 * 每日闹钟调度器
 * 使用 AlarmManager.setAlarmClock 设置每天固定时间的系统级闹钟
 */
public class AlarmScheduler {

    /**
     * 检查是否拥有精确闹钟权限（Android 12+ 需要）
     */
    public static boolean hasExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /**
     * 获取跳转到精确闹钟权限设置页面的 Intent（Android 12+）
     */
    public static Intent getExactAlarmSettingsIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData(android.net.Uri.parse("package:" + context.getPackageName()));
        }
        return null;
    }

    /**
     * 引导用户开启必要的权限（持久弹窗，带关闭按钮）
     * @param activity Activity 上下文（用于弹窗）
     */
    public static void showPermissionGuidance(android.app.Activity activity) {
        if (activity == null || activity.isFinishing()) return;

        StringBuilder tips = new StringBuilder();
        tips.append("1. 长按此App → 应用信息 → 设置自启动/后台弹窗");
        tips.append("\n2. 系统设置 → 电池 → 忽略电池优化");
        tips.append("\n3. 多任务界面 → 锁定此App（下拉加锁）");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasExactAlarmPermission(activity)) {
                tips.append("\n4. 授予精确闹钟权限（将自动跳转）");
            }
        }

        new AlertDialog.Builder(activity)
                .setTitle("闹钟已注册")
                .setMessage("如需准点响铃请完成以下设置：\n\n" + tips.toString())
                .setPositiveButton("知道了", null)
                .show();
    }

    /**
     * 为一个用药计划的所有提醒时间设置每日闹钟
     */
    public static void schedulePlanAlarms(Context context, long planId, String drugName,
                                          String dosage, String remark,
                                          List<String> remindTimes,
                                          String startDate, String endDate) {
        try {
            cancelPlanAlarms(context, planId);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            for (int i = 0; i < remindTimes.size(); i++) {
                String[] parts = remindTimes.get(i).split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);
                int order = i + 1;
                int total = remindTimes.size();

                // 触发闹钟的 Intent
                Intent intent = new Intent(context, MedicationAlarmReceiver.class);
                intent.setAction("ACTION_ALARM");
                int requestCode = (int) (planId * 100 + i);
                intent.putExtra("plan_id", planId);
                intent.putExtra("drug_name", drugName);
                intent.putExtra("dosage", dosage != null ? dosage : "");
                intent.putExtra("remark", remark != null ? remark : "");
                intent.putExtra("scheduled_time", remindTimes.get(i));
                intent.putExtra("order", order);
                intent.putExtra("total", total);

                PendingIntent operation = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                                : PendingIntent.FLAG_UPDATE_CURRENT);

                // 触发时间
                Calendar triggerCal = Calendar.getInstance();
                triggerCal.set(Calendar.HOUR_OF_DAY, hour);
                triggerCal.set(Calendar.MINUTE, minute);
                triggerCal.set(Calendar.SECOND, 0);
                triggerCal.set(Calendar.MILLISECOND, 0);
                long triggerMillis = triggerCal.getTimeInMillis();
                if (triggerMillis <= System.currentTimeMillis()) {
                    triggerCal.add(Calendar.DAY_OF_YEAR, 1);
                    triggerMillis = triggerCal.getTimeInMillis();
                }

                // setAlarmClock — 系统级闹钟，显示在系统时钟中
                // AlarmClockInfo(triggerTime, showIntent) — showIntent 可为 null
                // setAlarmClock(info, operation) — operation 是实际触发的 PendingIntent
                AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager.AlarmClockInfo(
                        triggerMillis, null);
                alarmManager.setAlarmClock(alarmInfo, operation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取消一个用药计划的所有闹钟
     */
    public static void cancelPlanAlarms(Context context, long planId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            for (int i = 0; i < 24; i++) {
                int requestCode = (int) (planId * 100 + i);
                Intent intent = new Intent(context, MedicationAlarmReceiver.class);
                intent.setAction("ACTION_ALARM");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, requestCode, intent,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
                                : PendingIntent.FLAG_NO_CREATE);
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

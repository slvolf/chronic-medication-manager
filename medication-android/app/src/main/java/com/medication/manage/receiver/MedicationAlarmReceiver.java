package com.medication.manage.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.medication.manage.MedicationApp;
import com.medication.manage.R;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.ui.dashboard.MainActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用药提醒广播接收器
 * 闹钟响铃 + 通知栏操作（关闭/稍后提醒/已服药）
 */
public class MedicationAlarmReceiver extends BroadcastReceiver {

    // 正在播放的铃声，用于关闭
    private static Ringtone sCurrentRingtone = null;
    private static Vibrator sVibrator = null;
    // 当前通知的 notifyId（用于关闭通知）
    private static int sCurrentNotifyId = -1;
    private static Context sCurrentContext = null;

    public static final String ACTION_ALARM = "ACTION_ALARM";
    public static final String ACTION_DISMISS = "ACTION_DISMISS";
    public static final String ACTION_SNOOZE = "ACTION_SNOOZE";
    public static final String ACTION_TAKEN = "ACTION_TAKEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_ALARM.equals(action)) {
            handleAlarm(context, intent);
        } else if (ACTION_DISMISS.equals(action)) {
            stopAlarmSound();
            cancelCurrentNotification(context);
        } else if (ACTION_SNOOZE.equals(action)) {
            stopAlarmSound();
            cancelCurrentNotification(context);
            // 5分钟后再次响铃
            rescheduleMinutesLater(context, intent, 5);
            android.widget.Toast.makeText(context, "已推迟5分钟", android.widget.Toast.LENGTH_SHORT).show();
        } else if (ACTION_TAKEN.equals(action)) {
            stopAlarmSound();
            cancelCurrentNotification(context);
            // 调用 API 标记为已服药
            long recordId = intent.getLongExtra("record_id", -1);
            if (recordId != -1) {
                Map<String, Object> params = new HashMap<>();
                params.put("recordId", recordId);
                RetrofitClient.getInstance().getApiService().checkIn(params)
                        .enqueue(new Callback<com.medication.manage.model.Result<Void>>() {
                            @Override
                            public void onResponse(Call<com.medication.manage.model.Result<Void>> call,
                                                   Response<com.medication.manage.model.Result<Void>> response) {}
                            @Override
                            public void onFailure(Call<com.medication.manage.model.Result<Void>> call, Throwable t) {}
                        });
            }
        }
    }

    private void handleAlarm(Context context, Intent intent) {
        String drugName = intent.getStringExtra("drug_name");
        String dosage = intent.getStringExtra("dosage");
        String remark = intent.getStringExtra("remark");
        String scheduledTime = intent.getStringExtra("scheduled_time");
        int order = intent.getIntExtra("order", 0);
        int total = intent.getIntExtra("total", 0);
        long planId = intent.getLongExtra("plan_id", -1);
        long recordId = intent.getLongExtra("record_id", -1);

        String title = "用药提醒";
        if (drugName != null) title = "用药提醒 - " + drugName;

        StringBuilder body = new StringBuilder();
        if (dosage != null && !dosage.isEmpty()) body.append(dosage).append(" ");
        if (remark != null && !remark.isEmpty()) body.append(remark).append(" ");
        if (total > 0) body.append("第").append(order).append("次/共").append(total).append("次");
        if (scheduledTime != null) body.append(" (").append(scheduledTime).append(")");

        // 播放铃声 + 震动
        playAlarmSound(context);
        playAlarmVibrate(context);

        // 显示带操作按钮的不可滑动通知
        showAlarmNotification(context, title, body.toString().trim(), planId, order, recordId, intent);

        // 重新调度明天的同一时间
        rescheduleNextDay(context, intent, planId, order);
    }

    /**
     * 显示闹钟通知（持续不可滑动，带关闭/稍后/已服药三个按钮）
     */
    private void showAlarmNotification(Context context, String title, String content,
                                        long planId, int notifyId, long recordId,
                                        Intent originalIntent) {
        sCurrentNotifyId = notifyId + (int) planId;
        sCurrentContext = context;

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent openPending = PendingIntent.getActivity(
                context, (int) planId, openIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // 关闭按钮
        Intent dismissIntent = new Intent(context, MedicationAlarmReceiver.class);
        dismissIntent.setAction(ACTION_DISMISS);
        PendingIntent dismissPending = PendingIntent.getBroadcast(
                context, (int) planId + 1, dismissIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // 稍后提醒按钮 — 需要携带原闹钟数据才能重新调度
        Intent snoozeIntent = new Intent(context, MedicationAlarmReceiver.class);
        snoozeIntent.setAction(ACTION_SNOOZE);
        if (originalIntent != null) {
            snoozeIntent.putExtras(originalIntent);
        }
        PendingIntent snoozePending = PendingIntent.getBroadcast(
                context, (int) planId + 2, snoozeIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MedicationApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_plan)
                .setContentTitle(title)
                .setContentText(content)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)              // 不可滑动删除，必须点按钮关闭
                .setFullScreenIntent(openPending, true)
                .setContentIntent(openPending)
                .addAction(R.drawable.ic_plan, "关闭", dismissPending)
                .addAction(R.drawable.ic_plan, "稍后5分钟", snoozePending)
                .setAutoCancel(false);

        // 如果有 recordId，加一个"已服药"按钮
        if (recordId != -1) {
            Intent takenIntent = new Intent(context, MedicationAlarmReceiver.class);
            takenIntent.setAction(ACTION_TAKEN);
            takenIntent.putExtra("record_id", recordId);
            PendingIntent takenPending = PendingIntent.getBroadcast(
                    context, (int) planId + 3, takenIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_plan, "已服药", takenPending);
        }

        try {
            NotificationManagerCompat.from(context).notify(
                    notifyId + (int) planId, builder.build());
        } catch (SecurityException ignored) {}
    }

    /** 关闭铃声（安全版，不检查 isPlaying 避免卡死） */
    private static void stopAlarmSound() {
        try {
            if (sCurrentRingtone != null) {
                sCurrentRingtone.stop();
                sCurrentRingtone = null;
            }
        } catch (Exception ignored) {}
        try {
            if (sVibrator != null) {
                sVibrator.cancel();
                sVibrator = null;
            }
        } catch (Exception ignored) {}
    }

    /** 关闭当前通知 */
    private static void cancelCurrentNotification(Context context) {
        if (sCurrentNotifyId != -1 && context != null) {
            try {
                NotificationManagerCompat.from(context).cancel(sCurrentNotifyId);
            } catch (Exception ignored) {}
            sCurrentNotifyId = -1;
            sCurrentContext = null;
        }
    }

    private void playAlarmSound(Context context) {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmUri);
            if (ringtone != null) {
                sCurrentRingtone = ringtone;
                ringtone.play();
            }
        } catch (Exception ignored) {}
    }

    private void playAlarmVibrate(Context context) {
        try {
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                sVibrator = vibrator;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(
                            new long[]{0, 500, 300, 500, 300, 1000}, 0));
                } else {
                    vibrator.vibrate(new long[]{0, 500, 300, 500, 300, 1000}, 0);
                }
            }
        } catch (Exception ignored) {}
    }

    private void rescheduleNextDay(Context context, Intent originalIntent, long planId, int order) {
        int requestCode = (int) (planId * 100 + order - 1);
        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtras(originalIntent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                        cal.getTimeInMillis(), null);
                alarmManager.setAlarmClock(info, pendingIntent);
            } catch (Exception ignored) {}
        }
    }

    /** 推迟 N 分钟后再次响铃 */
    private void rescheduleMinutesLater(Context context, Intent originalIntent, int minutes) {
        int requestCode = (int) (System.currentTimeMillis() % 100000);
        Intent intent = new Intent(context, MedicationAlarmReceiver.class);
        intent.setAction(ACTION_ALARM);
        intent.putExtras(originalIntent);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                        cal.getTimeInMillis(), null);
                alarmManager.setAlarmClock(info, pendingIntent);
            } catch (Exception ignored) {}
        }
    }
}

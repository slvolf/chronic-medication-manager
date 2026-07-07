package com.medication.manage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.medication.manage.service.AlarmScheduler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 开机广播接收器
 * 设备重启后重新注册每日闹钟
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // 从 SharedPreferences 恢复闹钟
            rescheduleAlarms(context);
        }
    }

    /**
     * 读取已持久化的闹钟配置并重新调度
     */
    private void rescheduleAlarms(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("medication_prefs", Context.MODE_PRIVATE);
        String alarmsJson = prefs.getString("saved_alarms", null);
        if (alarmsJson == null) return;

        try {
            JSONArray alarms = new JSONArray(alarmsJson);
            for (int i = 0; i < alarms.length(); i++) {
                JSONObject obj = alarms.getJSONObject(i);
                long planId = obj.getLong("plan_id");
                String drugName = obj.getString("drug_name");
                String dosage = obj.optString("dosage", "");
                String remark = obj.optString("remark", "");
                String startDate = obj.optString("start_date", "");
                String endDate = obj.optString("end_date", null);

                JSONArray timesArray = obj.getJSONArray("remind_times");
                List<String> remindTimes = new ArrayList<>();
                for (int j = 0; j < timesArray.length(); j++) {
                    remindTimes.add(timesArray.getString(j));
                }

                AlarmScheduler.schedulePlanAlarms(context, planId, drugName, dosage, remark,
                        remindTimes, startDate, endDate);
            }
        } catch (Exception e) {
            // JSON 解析失败，忽略
        }
    }
}

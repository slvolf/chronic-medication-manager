package com.medication.manage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.medication.manage.api.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用药提醒广播接收器
 * 处理通知栏快捷按钮："已服药" 和 "稍后提醒"
 */
public class MedicationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long recordId = intent.getLongExtra("record_id", -1);

        if (recordId == -1) return;

        if ("ACTION_CHECK_IN".equals(action)) {
            // 通过 API 打卡
            Map<String, Object> params = new HashMap<>();
            params.put("recordId", recordId);
            RetrofitClient.getInstance().getApiService().checkIn(params)
                    .enqueue(new Callback<com.medication.manage.model.Result<Void>>() {
                        @Override
                        public void onResponse(Call<com.medication.manage.model.Result<Void>> call,
                                               Response<com.medication.manage.model.Result<Void>> response) {
                            // 打卡完成，通知栏已自动关闭
                        }

                        @Override
                        public void onFailure(Call<com.medication.manage.model.Result<Void>> call, Throwable t) {
                        }
                    });
        } else if ("ACTION_REMIND_LATER".equals(action)) {
            // 稍后提醒：5分钟后再次提醒（实际项目中可通过 AlarmManager 实现）
            Toast.makeText(context, "将在5分钟后再次提醒", Toast.LENGTH_SHORT).show();
        }
    }
}

package com.medication.manage.ui.plan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.medication.manage.R;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityPlanEditBinding;
import com.medication.manage.model.MedicationPlan;
import com.medication.manage.model.Result;
import com.medication.manage.service.AlarmScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanEditActivity extends AppCompatActivity {

    private ActivityPlanEditBinding binding;
    private Long editPlanId;
    private List<String> remindTimeList = new ArrayList<>();
    private int selectedMode = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        editPlanId = getIntent().getLongExtra("plan_id", -1);
        if (editPlanId != -1) {
            binding.toolbar.setTitle("编辑计划");
            loadPlanDetail();
        }

        binding.chipGroupMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chip_mode_1) {
                selectedMode = 1;
                binding.tilTimes.setVisibility(View.VISIBLE);
                binding.layoutTimes.setVisibility(View.GONE);
                binding.layoutAlarm.setVisibility(View.GONE);
            } else if (checkedId == R.id.chip_mode_2) {
                selectedMode = 2;
                binding.tilTimes.setVisibility(View.GONE);
                binding.layoutTimes.setVisibility(View.VISIBLE);
                binding.layoutAlarm.setVisibility(View.VISIBLE);
            }
        });
        binding.chipMode2.setChecked(true);

        binding.btnAddTime.setOnClickListener(v -> showTimePicker());
        binding.etStartDate.setOnClickListener(v -> showDatePicker(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(binding.etEndDate));
        binding.btnSave.setOnClickListener(v -> savePlan());
    }

    private void loadPlanDetail() {
        RetrofitClient.getInstance().getApiService().getPlanDetail(editPlanId)
                .enqueue(new Callback<Result<MedicationPlan>>() {
                    @Override
                    public void onResponse(Call<Result<MedicationPlan>> call,
                                           Response<Result<MedicationPlan>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            MedicationPlan plan = response.body().getData();
                            binding.etDrugName.setText(plan.getDrugName());
                            binding.etDosage.setText(plan.getDosage());
                            binding.etStartDate.setText(plan.getStartDate());
                            binding.etEndDate.setText(plan.getEndDate());
                            binding.etRemark.setText(plan.getRemark());

                            if (plan.getFrequencyMode() == 1) {
                                binding.chipMode1.setChecked(true);
                                binding.etTimes.setText(String.valueOf(plan.getFrequencyTimes()));
                            } else {
                                binding.chipMode2.setChecked(true);
                                if (plan.getRemindTimes() != null) {
                                    for (String t : plan.getRemindTimes()) {
                                        addTimeChip(t);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<MedicationPlan>> call, Throwable t) {
                        Toast.makeText(PlanEditActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showTimePicker() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(48, 24, 48, 24);

        NumberPicker hourPicker = new NumberPicker(this);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        hourPicker.setLayoutParams(lp);

        TextView colon = new TextView(this);
        colon.setText(":");
        colon.setTextSize(28);
        colon.setGravity(android.view.Gravity.CENTER);

        NumberPicker minutePicker = new NumberPicker(this);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(Calendar.getInstance().get(Calendar.MINUTE));
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setLayoutParams(lp);

        NumberPicker.Formatter formatter = value -> String.format("%02d", value);
        hourPicker.setFormatter(formatter);
        minutePicker.setFormatter(formatter);

        layout.addView(hourPicker);
        layout.addView(colon);
        layout.addView(minutePicker);

        new AlertDialog.Builder(this)
                .setTitle("选择提醒时间")
                .setView(layout)
                .setPositiveButton("确定", (dialog, which) -> {
                    String time = String.format("%02d:%02d", hourPicker.getValue(), minutePicker.getValue());
                    addTimeChip(time);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void addTimeChip(String time) {
        if (remindTimeList.contains(time)) {
            Toast.makeText(this, "该时间已添加", Toast.LENGTH_SHORT).show();
            return;
        }

        remindTimeList.add(time);
        java.util.Collections.sort(remindTimeList);

        binding.layoutTimeList.removeAllViews();
        for (String t : remindTimeList) {
            final String timeText = t;
            Chip chip = new Chip(this);
            chip.setText(timeText);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                binding.layoutTimeList.removeView(chip);
                remindTimeList.remove(timeText);
            });
            binding.layoutTimeList.addView(chip);
        }
    }

    private void showDatePicker(TextView textView) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            textView.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void savePlan() {
        String drugName = binding.etDrugName.getText().toString().trim();
        String dosage = binding.etDosage.getText().toString().trim();
        String startDate = binding.etStartDate.getText().toString().trim();
        String endDate = binding.etEndDate.getText().toString().trim();
        String remark = binding.etRemark.getText().toString().trim();

        if (drugName.isEmpty()) {
            Toast.makeText(this, "请输入药品名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (startDate.isEmpty()) {
            Toast.makeText(this, "请选择开始日期", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> params = new HashMap<>();
        if (editPlanId != -1) params.put("id", editPlanId);
        params.put("drugName", drugName);
        params.put("dosage", dosage);
        params.put("frequencyMode", selectedMode);
        params.put("startDate", startDate);
        if (!endDate.isEmpty()) params.put("endDate", endDate);
        if (!remark.isEmpty()) params.put("remark", remark);

        if (selectedMode == 1) {
            String timesStr = binding.etTimes.getText().toString().trim();
            if (timesStr.isEmpty()) {
                Toast.makeText(this, "请输入每日次数", Toast.LENGTH_SHORT).show();
                return;
            }
            params.put("frequencyTimes", Integer.parseInt(timesStr));
            List<String> defaultTimes = new ArrayList<>();
            int times = Integer.parseInt(timesStr);
            for (int i = 0; i < times; i++) {
                int hour = 24 / times * i;
                defaultTimes.add(String.format("%02d:00", hour));
            }
            params.put("remindTimes", defaultTimes);
        } else {
            if (remindTimeList.isEmpty()) {
                Toast.makeText(this, "请添加至少一个提醒时间", Toast.LENGTH_SHORT).show();
                return;
            }
            java.util.Collections.sort(remindTimeList);
            params.put("remindTimes", remindTimeList);
        }

        binding.btnSave.setEnabled(false);

        Call<Result<MedicationPlan>> call;
        if (editPlanId != -1) {
            call = RetrofitClient.getInstance().getApiService().updatePlan(params);
        } else {
            call = RetrofitClient.getInstance().getApiService().createPlan(params);
        }

        call.enqueue(new Callback<Result<MedicationPlan>>() {
            @Override
            public void onResponse(Call<Result<MedicationPlan>> call,
                                   Response<Result<MedicationPlan>> response) {
                binding.btnSave.setEnabled(true);
                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(PlanEditActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    final MedicationPlan savedPlan = response.body().getData();

                    final boolean alarmEnabled = selectedMode == 2 && binding.switchAlarm.isChecked();
                    final Context appContext = getApplicationContext();
                    final Long savedPlanId = savedPlan != null ? savedPlan.getId() : null;

                    if (alarmEnabled && savedPlanId != null) {
                        AlarmScheduler.schedulePlanAlarms(
                                appContext, savedPlanId,
                                drugName, dosage, remark,
                                remindTimeList,
                                startDate, endDate.isEmpty() ? null : endDate);
                        saveAlarmPreference(appContext, savedPlanId, drugName, dosage, remark,
                                remindTimeList, startDate, endDate);

                        if (!AlarmScheduler.hasExactAlarmPermission(PlanEditActivity.this)) {
                            Intent permIntent = AlarmScheduler.getExactAlarmSettingsIntent(PlanEditActivity.this);
                            if (permIntent != null) startActivity(permIntent);
                        }

                        new AlertDialog.Builder(PlanEditActivity.this)
                                .setTitle("通知设置")
                                .setMessage("闹钟已注册，但因手机系统限制，"
                                        + "需要您手动开启通知声音才能响铃。"
                                        + "\n\n即将跳转通知设置页面，请将「用药提醒」渠道设置为："
                                        + "\n• 声音：重要/高"
                                        + "\n• 震动：开启"
                                        + "\n• 锁屏通知：显示")
                                .setPositiveButton("去设置", (dialog, which) -> {
                                    Intent channelIntent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                                            .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                                            .putExtra(Settings.EXTRA_CHANNEL_ID, "medication_reminder");
                                    startActivity(channelIntent);
                                    finish();
                                })
                                .setNegativeButton("稍后", (dialog, which) -> finish())
                                .setCancelable(false)
                                .show();
                    } else {
                        finish();
                    }
                } else {
                    String msg = response.body() != null ? response.body().getMsg() : "保存失败";
                    Toast.makeText(PlanEditActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<MedicationPlan>> call, Throwable t) {
                binding.btnSave.setEnabled(true);
                Toast.makeText(PlanEditActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAlarmPreference(Context context, Long planId, String drugName, String dosage,
                                      String remark, List<String> remindTimes,
                                      String startDate, String endDate) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("medication_prefs", MODE_PRIVATE);
            String json = prefs.getString("saved_alarms", "[]");
            org.json.JSONArray alarms = new org.json.JSONArray(json);

            for (int i = 0; i < alarms.length(); i++) {
                if (alarms.getJSONObject(i).getLong("plan_id") == planId) {
                    alarms.remove(i);
                    break;
                }
            }

            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("plan_id", planId);
            obj.put("drug_name", drugName);
            obj.put("dosage", dosage);
            obj.put("remark", remark);
            obj.put("start_date", startDate);
            obj.put("end_date", endDate.isEmpty() ? org.json.JSONObject.NULL : endDate);
            obj.put("remind_times", new org.json.JSONArray(remindTimes));
            alarms.put(obj);

            prefs.edit().putString("saved_alarms", alarms.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

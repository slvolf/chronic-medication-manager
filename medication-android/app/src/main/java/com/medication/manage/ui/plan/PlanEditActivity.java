package com.medication.manage.ui.plan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 新增/编辑用药计划界面
 */
public class PlanEditActivity extends AppCompatActivity {

    private ActivityPlanEditBinding binding;
    private Long editPlanId;                    // 编辑模式时的计划ID
    private List<String> remindTimeList = new ArrayList<>(); // 提醒时间列表
    private int selectedMode = 2;               // 默认固定时间模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 检查是否编辑模式
        editPlanId = getIntent().getLongExtra("plan_id", -1);
        if (editPlanId != -1) {
            binding.toolbar.setTitle("编辑计划");
            loadPlanDetail();
        }

        // 频次模式切换
        binding.chipGroupMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = group.getCheckedChipId();
            if (checkedId == R.id.chip_mode_1) {
                selectedMode = 1;
                binding.tilTimes.setVisibility(View.VISIBLE);
                binding.layoutTimes.setVisibility(View.GONE);
            } else if (checkedId == R.id.chip_mode_2) {
                selectedMode = 2;
                binding.tilTimes.setVisibility(View.GONE);
                binding.layoutTimes.setVisibility(View.VISIBLE);
            }
        });
        binding.chipMode2.setChecked(true); // 默认固定时间

        // 添加提醒时间
        binding.btnAddTime.setOnClickListener(v -> showTimePicker());

        // 日期选择
        binding.etStartDate.setOnClickListener(v -> showDatePicker(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(binding.etEndDate));

        // 保存
        binding.btnSave.setOnClickListener(v -> savePlan());
    }

    /**
     * 加载计划详情（编辑模式）
     */
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

    /**
     * 显示时间选择器（上下滑动滚轮样式，类似闹钟界面）
     */
    private void showTimePicker() {
        // 创建布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(48, 24, 48, 24);

        // 小时选择器 (0-23)
        NumberPicker hourPicker = new NumberPicker(this);
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        hourPicker.setValue(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        hourPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        hourPicker.setLayoutParams(lp);

        // 冒号分隔
        TextView colon = new TextView(this);
        colon.setText(":");
        colon.setTextSize(28);
        colon.setGravity(android.view.Gravity.CENTER);

        // 分钟选择器 (0-59)
        NumberPicker minutePicker = new NumberPicker(this);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setValue(Calendar.getInstance().get(Calendar.MINUTE));
        minutePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutePicker.setLayoutParams(lp);

        // 格式化分钟为两位数显示
        NumberPicker.Formatter formatter = value -> String.format("%02d", value);
        hourPicker.setFormatter(formatter);
        minutePicker.setFormatter(formatter);

        layout.addView(hourPicker);
        layout.addView(colon);
        layout.addView(minutePicker);

        // 弹出对话框
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

    /**
     * 添加提醒时间 Chip
     */
    private void addTimeChip(String time) {
        if (remindTimeList.contains(time)) {
            Toast.makeText(this, "该时间已添加", Toast.LENGTH_SHORT).show();
            return;
        }

        // 按时间顺序插入
        remindTimeList.add(time);
        java.util.Collections.sort(remindTimeList);

        // 重新绘制所有 Chip（保持界面与列表顺序一致）
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

    /**
     * 显示日期选择器
     */
    private void showDatePicker(TextView textView) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            textView.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 保存用药计划
     */
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
        if (editPlanId != -1) {
            params.put("id", editPlanId);
        }
        params.put("drugName", drugName);
        params.put("dosage", dosage);
        params.put("frequencyMode", selectedMode);
        params.put("startDate", startDate);
        if (!endDate.isEmpty()) {
            params.put("endDate", endDate);
        }
        if (!remark.isEmpty()) {
            params.put("remark", remark);
        }

        if (selectedMode == 1) {
            String timesStr = binding.etTimes.getText().toString().trim();
            if (timesStr.isEmpty()) {
                Toast.makeText(this, "请输入每日次数", Toast.LENGTH_SHORT).show();
                return;
            }
            params.put("frequencyTimes", Integer.parseInt(timesStr));
            // 生成默认提醒时间（从0点均匀分布）
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
            // 发送前确保按时间排序
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
                    finish();
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
}

package com.medication.manage.ui.record;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.medication.manage.adapter.MedicationRecordAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityHistoryBinding;
import com.medication.manage.model.MedicationRecord;
import com.medication.manage.model.Result;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 历史记录界面
 * 按日期查看用药打卡历史
 */
public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private MedicationRecordAdapter adapter;
    private String currentDate; // 当前查看的日期 "yyyy-MM-dd"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // 初始化日期为今天
        currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        binding.tvSelectedDate.setText(currentDate);

        // RecyclerView（只读模式，不显示操作按钮）
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationRecordAdapter(null, null, null);
        binding.rvHistory.setAdapter(adapter);

        // 点击日期选择
        binding.tvSelectedDate.setOnClickListener(v -> showDatePicker());

        loadRecords();
    }

    /**
     * 显示日期选择器
     */
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            currentDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.tvSelectedDate.setText(currentDate);
            loadRecords();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * 加载选中日期的记录
     */
    private void loadRecords() {
        RetrofitClient.getInstance().getApiService().getRecordsByDate(currentDate)
                .enqueue(new Callback<Result<List<MedicationRecord>>>() {
                    @Override
                    public void onResponse(Call<Result<List<MedicationRecord>>> call,
                                           Response<Result<List<MedicationRecord>>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            List<MedicationRecord> records = response.body().getData();
                            adapter.updateData(records);
                            binding.layoutEmpty.setVisibility(
                                    records == null || records.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<List<MedicationRecord>>> call, Throwable t) {
                        Toast.makeText(HistoryActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.medication.manage.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.medication.manage.R;
import com.medication.manage.adapter.MedicationRecordAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityMainBinding;
import com.medication.manage.model.ComplianceRate;
import com.medication.manage.model.MedicationRecord;
import com.medication.manage.model.Result;
import com.medication.manage.ui.login.LoginActivity;
import com.medication.manage.ui.plan.PlanListActivity;
import com.medication.manage.ui.record.HistoryActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 主界面
 * 展示今日依从率、近7天平均依从率、今日用药列表
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MedicationRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 RecyclerView
        binding.rvTodayRecords.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationRecordAdapter(null,
                record -> checkIn(record),
                record -> markMissed(record));
        binding.rvTodayRecords.setAdapter(adapter);

        // 跳转历史记录
        binding.btnHistory.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        // 底部导航
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_plans) {
                startActivity(new Intent(this, PlanListActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                // 退出登录
                getSharedPreferences("medication_prefs", MODE_PRIVATE)
                        .edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * 加载首页所有数据
     */
    private void loadData() {
        loadTodayCompliance();
        loadWeeklyCompliance();
        loadTodayRecords();
    }

    /**
     * 加载今日依从率
     */
    private void loadTodayCompliance() {
        RetrofitClient.getInstance().getApiService().getTodayCompliance()
                .enqueue(new Callback<Result<ComplianceRate>>() {
                    @Override
                    public void onResponse(Call<Result<ComplianceRate>> call,
                                           Response<Result<ComplianceRate>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            ComplianceRate rate = response.body().getData();
                            binding.tvTodayRate.setText(rate.getRateText());
                            binding.tvTodayTaken.setText("已服药: " + rate.getActualCount());
                            binding.tvTodayMissed.setText("漏服: " + rate.getMissedCount());
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<ComplianceRate>> call, Throwable t) {
                        // 静默失败，不阻塞UI
                    }
                });
    }

    /**
     * 加载近7天依从率
     */
    private void loadWeeklyCompliance() {
        RetrofitClient.getInstance().getApiService().getWeeklyCompliance()
                .enqueue(new Callback<Result<ComplianceRate>>() {
                    @Override
                    public void onResponse(Call<Result<ComplianceRate>> call,
                                           Response<Result<ComplianceRate>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            ComplianceRate rate = response.body().getData();
                            binding.tvWeeklyRate.setText(rate.getRateText());
                            binding.tvWeeklyTaken.setText("已服药: " + rate.getActualCount());
                            binding.tvWeeklyMissed.setText("漏服: " + rate.getMissedCount());
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<ComplianceRate>> call, Throwable t) {
                    }
                });
    }

    /**
     * 加载今日用药记录
     */
    private void loadTodayRecords() {
        RetrofitClient.getInstance().getApiService().getTodayRecords()
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
                    }
                });
    }

    /**
     * 打卡
     */
    private void checkIn(MedicationRecord record) {
        java.util.HashMap<String, Object> params = new java.util.HashMap<>();
        params.put("recordId", record.getId());

        RetrofitClient.getInstance().getApiService().checkIn(params)
                .enqueue(new Callback<Result<Void>>() {
                    @Override
                    public void onResponse(Call<Result<Void>> call,
                                           Response<Result<Void>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(MainActivity.this, "打卡成功", Toast.LENGTH_SHORT).show();
                            loadTodayRecords();
                            loadTodayCompliance();
                        } else {
                            Toast.makeText(MainActivity.this, "打卡失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<Void>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 标记漏服
     */
    private void markMissed(MedicationRecord record) {
        RetrofitClient.getInstance().getApiService().markMissed(record.getId())
                .enqueue(new Callback<Result<Void>>() {
                    @Override
                    public void onResponse(Call<Result<Void>> call,
                                           Response<Result<Void>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            loadTodayRecords();
                            loadTodayCompliance();
                        }
                    }

                    @Override
                    public void onFailure(Call<Result<Void>> call, Throwable t) {
                    }
                });
    }
}

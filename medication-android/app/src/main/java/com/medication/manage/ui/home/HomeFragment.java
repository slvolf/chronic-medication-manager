package com.medication.manage.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.medication.manage.R;
import com.medication.manage.adapter.MedicationRecordAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.FragmentHomeBinding;
import com.medication.manage.model.ComplianceRate;
import com.medication.manage.model.MedicationRecord;
import com.medication.manage.model.Result;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 首页 Fragment — 今日依从率 + 近7天依从率 + 今日用药列表
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private MedicationRecordAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvTodayRecords.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicationRecordAdapter(null,
                record -> checkIn(record),
                record -> markMissed(record));
        binding.rvTodayRecords.setAdapter(adapter);

        // 跳转历史 — 通过 Activity 的导航切换
        binding.btnHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((com.medication.manage.ui.dashboard.MainActivity) getActivity())
                        .switchToTab(R.id.nav_history);
            }
        });

        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        loadTodayCompliance();
        loadWeeklyCompliance();
        loadTodayRecords();
    }

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
                    public void onFailure(Call<Result<ComplianceRate>> call, Throwable t) {}
                });
    }

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
                    public void onFailure(Call<Result<ComplianceRate>> call, Throwable t) {}
                });
    }

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
                    public void onFailure(Call<Result<List<MedicationRecord>>> call, Throwable t) {}
                });
    }

    private void checkIn(MedicationRecord record) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("recordId", record.getId());
        RetrofitClient.getInstance().getApiService().checkIn(params)
                .enqueue(new Callback<Result<Void>>() {
                    @Override
                    public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            loadTodayRecords();
                            loadTodayCompliance();
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<Void>> call, Throwable t) {}
                });
    }

    private void markMissed(MedicationRecord record) {
        RetrofitClient.getInstance().getApiService().markMissed(record.getId())
                .enqueue(new Callback<Result<Void>>() {
                    @Override
                    public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            loadTodayRecords();
                            loadTodayCompliance();
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<Void>> call, Throwable t) {}
                });
    }
}

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
import com.medication.manage.adapter.HomePageAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.model.ComplianceRate;
import com.medication.manage.model.MedicationRecord;
import com.medication.manage.model.Result;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private HomePageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        androidx.recyclerview.widget.RecyclerView rv = view.findViewById(R.id.rv_home);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new HomePageAdapter(
                record -> checkIn(record),
                record -> markMissed(record));
        rv.setAdapter(adapter);

        // 历史记录按钮 — adapter 首次 bind 后会获得 header 引用
        adapter.setOnHistoryClickListener(v -> {
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
                            if (rate != null) {
                                adapter.updateTodayCompliance(rate.getRateText(),
                                        rate.getActualCount(), rate.getMissedCount());
                            }
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
                            if (rate != null) {
                                adapter.updateWeeklyCompliance(rate.getRateText(),
                                        rate.getActualCount(), rate.getMissedCount());
                            }
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
                            adapter.setRecords(records);
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<List<MedicationRecord>>> call, Throwable t) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "网络异常",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
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

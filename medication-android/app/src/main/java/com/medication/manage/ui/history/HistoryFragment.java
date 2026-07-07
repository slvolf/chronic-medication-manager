package com.medication.manage.ui.history;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
 * 历史记录 Fragment — 按日期查看打卡历史
 */
public class HistoryFragment extends Fragment {

    private ActivityHistoryBinding binding;
    private MedicationRecordAdapter adapter;
    private String currentDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 隐藏此 Fragment 中的 Toolbar（由 MainActivity 统一管理）
        binding.toolbar.setVisibility(View.GONE);

        currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        binding.tvSelectedDate.setText(currentDate);

        binding.rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicationRecordAdapter(null, null, null);
        binding.rvHistory.setAdapter(adapter);

        binding.tvSelectedDate.setOnClickListener(v -> showDatePicker());

        loadRecords();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecords();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            currentDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            binding.tvSelectedDate.setText(currentDate);
            loadRecords();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

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
                    public void onFailure(Call<Result<List<MedicationRecord>>> call, Throwable t) {}
                });
    }
}

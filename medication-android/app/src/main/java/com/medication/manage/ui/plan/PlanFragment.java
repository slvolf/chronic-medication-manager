package com.medication.manage.ui.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.medication.manage.R;
import com.medication.manage.adapter.MedicationPlanAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityPlanListBinding;
import com.medication.manage.model.MedicationPlan;
import com.medication.manage.model.Result;
import com.medication.manage.service.AlarmScheduler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用药计划列表 Fragment
 */
public class PlanFragment extends Fragment {

    private ActivityPlanListBinding binding;
    private MedicationPlanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityPlanListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 隐藏此 Fragment 中的 Toolbar（由 MainActivity 统一管理）
        binding.toolbar.setVisibility(View.GONE);

        binding.rvPlans.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MedicationPlanAdapter(
                null,
                plan -> {
                    Intent intent = new Intent(getActivity(), PlanEditActivity.class);
                    intent.putExtra("plan_id", plan.getId());
                    startActivity(intent);
                },
                plan -> deletePlan(plan)
        );
        binding.rvPlans.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), PlanEditActivity.class));
        });

        loadPlans();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPlans();
    }

    private void loadPlans() {
        RetrofitClient.getInstance().getApiService().getPlanList()
                .enqueue(new Callback<Result<List<MedicationPlan>>>() {
                    @Override
                    public void onResponse(Call<Result<List<MedicationPlan>>> call,
                                           Response<Result<List<MedicationPlan>>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            List<MedicationPlan> plans = response.body().getData();
                            adapter.updateData(plans);
                            binding.layoutEmpty.setVisibility(
                                    plans == null || plans.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<List<MedicationPlan>>> call, Throwable t) {
                        if (getContext() != null) {
                            android.widget.Toast.makeText(getContext(), "加载计划列表失败", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deletePlan(MedicationPlan plan) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("确认删除")
                .setMessage("确定删除「" + plan.getDrugName() + "」的用药计划吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    RetrofitClient.getInstance().getApiService().deletePlan(plan.getId())
                            .enqueue(new Callback<Result<Void>>() {
                                @Override
                                public void onResponse(Call<Result<Void>> call,
                                                       Response<Result<Void>> response) {
                                    // 取消该计划的闹钟
                                    if (plan.getId() != null) {
                                        AlarmScheduler.cancelPlanAlarms(requireContext(), plan.getId());
                                    }
                                    loadPlans();
                                }
                                @Override
                                public void onFailure(Call<Result<Void>> call, Throwable t) {}
                            });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}

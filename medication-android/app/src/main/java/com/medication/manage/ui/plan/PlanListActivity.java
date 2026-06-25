package com.medication.manage.ui.plan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.medication.manage.adapter.MedicationPlanAdapter;
import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityPlanListBinding;
import com.medication.manage.model.MedicationPlan;
import com.medication.manage.model.Result;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 用药计划列表界面
 */
public class PlanListActivity extends AppCompatActivity {

    private ActivityPlanListBinding binding;
    private MedicationPlanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 返回按钮
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView
        binding.rvPlans.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationPlanAdapter(
                null,
                plan -> {
                    // 点击编辑
                    Intent intent = new Intent(this, PlanEditActivity.class);
                    intent.putExtra("plan_id", plan.getId());
                    startActivity(intent);
                },
                plan -> {
                    // 删除
                    deletePlan(plan);
                }
        );
        binding.rvPlans.setAdapter(adapter);

        // 新增按钮
        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, PlanEditActivity.class));
        });

        loadPlans();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlans();
    }

    /**
     * 加载用药计划列表
     */
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
                        Toast.makeText(PlanListActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * 删除用药计划
     */
    private void deletePlan(MedicationPlan plan) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("确定删除「" + plan.getDrugName() + "」的用药计划吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    RetrofitClient.getInstance().getApiService().deletePlan(plan.getId())
                            .enqueue(new Callback<Result<Void>>() {
                                @Override
                                public void onResponse(Call<Result<Void>> call,
                                                       Response<Result<Void>> response) {
                                    if (response.body() != null && response.body().isSuccess()) {
                                        Toast.makeText(PlanListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        loadPlans();
                                    }
                                }

                                @Override
                                public void onFailure(Call<Result<Void>> call, Throwable t) {
                                    Toast.makeText(PlanListActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}

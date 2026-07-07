package com.medication.manage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.manage.R;
import com.medication.manage.model.MedicationPlan;

import java.util.List;

/**
 * 用药计划列表适配器
 */
public class MedicationPlanAdapter extends RecyclerView.Adapter<MedicationPlanAdapter.ViewHolder> {

    private List<MedicationPlan> plans;
    private OnItemClickListener itemClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnItemClickListener {
        void onItemClick(MedicationPlan plan);
    }

    public interface OnDeleteClickListener {
        void onDelete(MedicationPlan plan);
    }

    public MedicationPlanAdapter(List<MedicationPlan> plans,
                                 OnItemClickListener itemClickListener,
                                 OnDeleteClickListener deleteClickListener) {
        this.plans = plans;
        this.itemClickListener = itemClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationPlan plan = plans.get(position);
        holder.tvDrugName.setText(plan.getDrugName() != null ? plan.getDrugName() : "");
        holder.tvDosage.setText(plan.getDosage() != null ? "剂量：" + plan.getDosage() : "");

        // 构建频次文字
        String freqText;
        if (plan.getFrequencyMode() == 1) {
            freqText = "每日" + plan.getFrequencyTimes() + "次";
        } else {
            StringBuilder times = new StringBuilder("每日：");
            if (plan.getRemindTimes() != null) {
                for (String t : plan.getRemindTimes()) {
                    times.append(t).append(" ");
                }
            }
            freqText = times.toString().trim();
        }
        holder.tvFrequency.setText(freqText);

        // 日期范围
        String dateRange = plan.getStartDate() != null ? plan.getStartDate() : "";
        if (plan.getEndDate() != null) {
            dateRange += " ~ " + plan.getEndDate();
        } else {
            dateRange += " ~ 长期";
        }
        holder.tvDateRange.setText(dateRange);

        // 点击编辑
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) itemClickListener.onItemClick(plan);
        });

        // 删除
        holder.ivDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) deleteClickListener.onDelete(plan);
        });
    }

    @Override
    public int getItemCount() {
        return plans == null ? 0 : plans.size();
    }

    public void updateData(List<MedicationPlan> newPlans) {
        this.plans = newPlans;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrugName, tvDosage, tvFrequency, tvDateRange;
        ImageView ivDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(R.id.tv_drug_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}

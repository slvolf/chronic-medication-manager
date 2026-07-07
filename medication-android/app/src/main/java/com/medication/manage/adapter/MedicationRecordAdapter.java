package com.medication.manage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.medication.manage.R;
import com.medication.manage.model.MedicationRecord;

import java.util.List;

/**
 * 今日用药记录列表适配器
 * 展示每条记录的药品名、剂量、时间和状态，提供打卡操作按钮
 */
public class MedicationRecordAdapter extends RecyclerView.Adapter<MedicationRecordAdapter.ViewHolder> {

    private List<MedicationRecord> records;
    private OnCheckInListener checkInListener;
    private OnMarkMissedListener missedListener;

    public interface OnCheckInListener {
        void onCheckIn(MedicationRecord record);
    }

    public interface OnMarkMissedListener {
        void onMarkMissed(MedicationRecord record);
    }

    public MedicationRecordAdapter(List<MedicationRecord> records,
                                   OnCheckInListener checkInListener,
                                   OnMarkMissedListener missedListener) {
        this.records = records;
        this.checkInListener = checkInListener;
        this.missedListener = missedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationRecord record = records.get(position);

        holder.tvDrugName.setText(record.getDrugName() != null ? record.getDrugName() : "");
        holder.tvDosage.setText(record.getDosage() != null ? record.getDosage() : "");
        holder.tvScheduledTime.setText(record.getScheduledTime() != null ? record.getScheduledTime() : "");

        // 根据状态设置颜色和文字
        int statusColor;
        String statusText;
        String actionText;

        switch (record.getStatus()) {
            case 0: // 待服药
                statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
                statusText = "待服药";
                actionText = "打卡";
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setText(actionText);
                holder.btnAction.setOnClickListener(v -> {
                    if (checkInListener != null) checkInListener.onCheckIn(record);
                });
                break;
            case 1: // 已服药
                statusColor = holder.itemView.getContext().getColor(R.color.status_taken);
                statusText = "已服药";
                holder.btnAction.setVisibility(View.GONE);
                break;
            case 2: // 漏服
                statusColor = holder.itemView.getContext().getColor(R.color.status_missed);
                statusText = "漏服";
                holder.btnAction.setVisibility(View.GONE);
                break;
            default:
                statusColor = holder.itemView.getContext().getColor(R.color.text_hint);
                statusText = "未知";
                holder.btnAction.setVisibility(View.GONE);
        }

        holder.viewStatusIndicator.setBackgroundColor(statusColor);
        holder.tvStatus.setTextColor(statusColor);
        holder.tvStatus.setText(statusText);
    }

    @Override
    public int getItemCount() {
        return records == null ? 0 : records.size();
    }

    /**
     * 更新数据
     */
    public void updateData(List<MedicationRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvDrugName, tvDosage, tvScheduledTime, tvStatus;
        Button btnAction;

        ViewHolder(View itemView) {
            super(itemView);
            viewStatusIndicator = itemView.findViewById(R.id.view_status_indicator);
            tvDrugName = itemView.findViewById(R.id.tv_drug_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvScheduledTime = itemView.findViewById(R.id.tv_scheduled_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}

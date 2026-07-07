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
 * 首页适配器 — 第一项是 Header（依从率卡片），后续项是用药记录
 */
public class HomePageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<MedicationRecord> records;
    private OnCheckInListener checkInListener;
    private OnMarkMissedListener missedListener;

    // Header 内需要更新的 View
    private TextView tvTodayRate, tvTodayTaken, tvTodayMissed;
    private TextView tvWeeklyRate, tvWeeklyTaken, tvWeeklyMissed;
    private Button btnHistory;
    private View layoutEmpty;

    public interface OnCheckInListener {
        void onCheckIn(MedicationRecord record);
    }
    public interface OnMarkMissedListener {
        void onMarkMissed(MedicationRecord record);
    }

    public HomePageAdapter(OnCheckInListener checkInListener,
                           OnMarkMissedListener missedListener) {
        this.checkInListener = checkInListener;
        this.missedListener = missedListener;
    }

    /** 设置今日用药记录列表 */
    public void setRecords(List<MedicationRecord> records) {
        this.records = records;
        notifyDataSetChanged();
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(records == null || records.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /** 获取 Header 视图，绑定后即可调用 update* 方法 */
    public void bindHeader(View headerView) {
        tvTodayRate = headerView.findViewById(R.id.tv_today_rate);
        tvTodayTaken = headerView.findViewById(R.id.tv_today_taken);
        tvTodayMissed = headerView.findViewById(R.id.tv_today_missed);
        tvWeeklyRate = headerView.findViewById(R.id.tv_weekly_rate);
        tvWeeklyTaken = headerView.findViewById(R.id.tv_weekly_taken);
        tvWeeklyMissed = headerView.findViewById(R.id.tv_weekly_missed);
        btnHistory = headerView.findViewById(R.id.btn_history);
        layoutEmpty = headerView.findViewById(R.id.layout_empty);
        // 应用之前设置的点击监听
        if (pendingHistoryListener != null && btnHistory != null) {
            btnHistory.setOnClickListener(pendingHistoryListener);
        }
    }

    public void updateTodayCompliance(String rate, int taken, int missed) {
        if (tvTodayRate != null) tvTodayRate.setText(rate);
        if (tvTodayTaken != null) tvTodayTaken.setText("已服药: " + taken);
        if (tvTodayMissed != null) tvTodayMissed.setText("漏服: " + missed);
    }

    public void updateWeeklyCompliance(String rate, int taken, int missed) {
        if (tvWeeklyRate != null) tvWeeklyRate.setText(rate);
        if (tvWeeklyTaken != null) tvWeeklyTaken.setText("已服药: " + taken);
        if (tvWeeklyMissed != null) tvWeeklyMissed.setText("漏服: " + missed);
    }

    private View.OnClickListener pendingHistoryListener;

    public void setOnHistoryClickListener(View.OnClickListener listener) {
        pendingHistoryListener = listener;
        if (btnHistory != null) btnHistory.setOnClickListener(listener);
    }

    public Button getHistoryButton() { return btnHistory; }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_HEADER : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return (records == null ? 0 : records.size()) + 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_home_header, parent, false);
            return new HeaderViewHolder(v);
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medication_record, parent, false);
        return new RecordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // Header — 绑定视图引用
            bindHeader(((HeaderViewHolder) holder).itemView);
            return;
        }
        if (position > 0 && records != null && position - 1 < records.size()) {
            MedicationRecord record = records.get(position - 1);
            RecordViewHolder rh = (RecordViewHolder) holder;

            rh.tvDrugName.setText(record.getDrugName() != null ? record.getDrugName() : "");
            rh.tvDosage.setText(record.getDosage() != null ? record.getDosage() : "");
            rh.tvScheduledTime.setText(record.getScheduledTime() != null ? record.getScheduledTime() : "");

            int statusColor;
            String statusText;
            boolean showAction;

            switch (record.getStatus()) {
                case 0:
                    statusColor = holder.itemView.getContext().getColor(R.color.status_pending);
                    statusText = "待服药";
                    showAction = true;
                    rh.btnAction.setText("打卡");
                    rh.btnAction.setOnClickListener(v -> {
                        if (checkInListener != null) checkInListener.onCheckIn(record);
                    });
                    break;
                case 1:
                    statusColor = holder.itemView.getContext().getColor(R.color.status_taken);
                    statusText = "已服药";
                    showAction = false;
                    break;
                default:
                    statusColor = holder.itemView.getContext().getColor(R.color.status_missed);
                    statusText = "漏服";
                    showAction = false;
            }

            rh.viewStatusIndicator.setBackgroundColor(statusColor);
            rh.tvStatus.setTextColor(statusColor);
            rh.tvStatus.setText(statusText);
            rh.btnAction.setVisibility(showAction ? View.VISIBLE : View.GONE);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) { super(itemView); }
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        View viewStatusIndicator;
        TextView tvDrugName, tvDosage, tvScheduledTime, tvStatus;
        Button btnAction;

        RecordViewHolder(View itemView) {
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

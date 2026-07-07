package com.medication.manage.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.medication.manage.api.RetrofitClient;
import com.medication.manage.model.Result;
import com.medication.manage.model.UserInfo;
import com.medication.manage.ui.login.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 个人中心 Fragment — 用户信息 + 退出登录
 */
public class ProfileFragment extends Fragment {

    private android.widget.TextView tvName, tvPhone;
    private com.google.android.material.button.MaterialButton btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        android.widget.LinearLayout root = new android.widget.LinearLayout(getContext());
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.CENTER);
        root.setPadding(32, 32, 32, 32);
        root.setBackgroundColor(0xFFF5F5F5);

        // 头像占位
        android.widget.TextView avatar = new android.widget.TextView(getContext());
        avatar.setLayoutParams(new ViewGroup.LayoutParams(96, 96));
        avatar.setTextSize(36);
        avatar.setTextColor(0xFFFFFFFF);
        avatar.setGravity(android.view.Gravity.CENTER);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        bg.setColor(0xFF2196F3);
        avatar.setBackground(bg);
        root.addView(avatar);

        // 间距
        root.addView(createSpacer(16));

        // 用户名
        tvName = new android.widget.TextView(getContext());
        tvName.setTextSize(20);
        tvName.setTextColor(0xFF212121);
        tvName.setGravity(android.view.Gravity.CENTER);
        root.addView(tvName);

        // 间距
        root.addView(createSpacer(8));

        // 手机号
        tvPhone = new android.widget.TextView(getContext());
        tvPhone.setTextSize(14);
        tvPhone.setTextColor(0xFF757575);
        tvPhone.setGravity(android.view.Gravity.CENTER);
        root.addView(tvPhone);

        // 间距
        root.addView(createSpacer(48));

        // 退出登录按钮
        btnLogout = new com.google.android.material.button.MaterialButton(getContext());
        btnLogout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btnLogout.setText("退出登录");
        btnLogout.setTextSize(16);
        btnLogout.setTextColor(0xFFFFFFFF);
        btnLogout.setBackgroundColor(0xFFF44336);
        btnLogout.setOnClickListener(v -> logout());
        root.addView(btnLogout);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserInfo();
    }

    private View createSpacer(int heightDp) {
        View spacer = new View(getContext());
        spacer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (heightDp * getResources().getDisplayMetrics().density)));
        return spacer;
    }

    private void loadUserInfo() {
        RetrofitClient.getInstance().getApiService().getUserInfo()
                .enqueue(new Callback<Result<UserInfo>>() {
                    @Override
                    public void onResponse(Call<Result<UserInfo>> call,
                                           Response<Result<UserInfo>> response) {
                        if (response.body() != null && response.body().isSuccess()) {
                            UserInfo info = response.body().getData();
                            tvName.setText(info.getName() != null ? info.getName() : "用户");
                            tvPhone.setText(info.getPhone() != null ? info.getPhone() : "");
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<UserInfo>> call, Throwable t) {}
                });
    }

    private void logout() {
        requireContext().getSharedPreferences("medication_prefs", android.content.Context.MODE_PRIVATE)
                .edit().clear().apply();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        if (getActivity() != null) getActivity().finish();
    }
}

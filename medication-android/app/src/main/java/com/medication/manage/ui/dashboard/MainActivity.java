package com.medication.manage.ui.dashboard;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.medication.manage.R;
import com.medication.manage.databinding.ActivityMainBinding;
import com.medication.manage.service.AlarmScheduler;
import com.medication.manage.ui.home.HomeFragment;
import com.medication.manage.ui.plan.PlanFragment;
import com.medication.manage.ui.history.HistoryFragment;
import com.medication.manage.ui.profile.ProfileFragment;

/**
 * 主界面 — 底部导航栏始终存在，四个页面通过 Fragment 切换
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // 通知权限请求（Android 13+）
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            switchFragment(R.id.nav_home);
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });

        // 启动时检查必要权限
        checkRequiredPermissions();
    }

    private void checkRequiredPermissions() {
        // 1. 通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // 2. 精确闹钟权限（Android 12+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!AlarmScheduler.hasExactAlarmPermission(this)) {
                new AlertDialog.Builder(this)
                        .setTitle("需要精确闹钟权限")
                        .setMessage("为了准时提醒您服药，请在系统设置中允许精确闹钟权限。")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                    .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("暂不", null)
                        .show();
            }
        }

        // 3. 电池优化豁免（引导用户手动关闭）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .setData(Uri.parse("package:" + getPackageName()));
            // 尝试请求忽略电池优化（系统可能不处理此 Intent）
            try {
                startActivity(intent);
            } catch (Exception ignored) {}
        }
    }

    public void switchToTab(int menuItemId) {
        binding.bottomNav.setSelectedItemId(menuItemId);
        switchFragment(menuItemId);
    }

    private void switchFragment(int menuItemId) {
        String title;
        Fragment fragment;

        if (menuItemId == R.id.nav_plans) {
            title = "用药计划";
            fragment = new PlanFragment();
        } else if (menuItemId == R.id.nav_history) {
            title = "历史记录";
            fragment = new HistoryFragment();
        } else if (menuItemId == R.id.nav_profile) {
            title = "我的";
            fragment = new ProfileFragment();
        } else {
            title = "用药管理";
            fragment = new HomeFragment();
        }

        binding.toolbar.setTitle(title);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commit();
    }
}

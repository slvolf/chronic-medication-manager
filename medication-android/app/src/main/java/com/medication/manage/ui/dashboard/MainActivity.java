package com.medication.manage.ui.dashboard;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.medication.manage.R;
import com.medication.manage.databinding.ActivityMainBinding;
import com.medication.manage.ui.home.HomeFragment;
import com.medication.manage.ui.plan.PlanFragment;
import com.medication.manage.ui.history.HistoryFragment;
import com.medication.manage.ui.profile.ProfileFragment;

/**
 * 主界面 — 底部导航栏始终存在，四个页面通过 Fragment 切换
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 如果是从登录页第一次进入，默认显示首页
        if (savedInstanceState == null) {
            switchFragment(R.id.nav_home);
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            switchFragment(item.getItemId());
            return true;
        });
    }

    /**
     * 切换 Fragment — 供外部 Fragment 调用（如 HomeFragment 跳转历史）
     */
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

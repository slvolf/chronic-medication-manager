package com.medication.manage.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityLoginBinding;
import com.medication.manage.model.LoginResponse;
import com.medication.manage.model.Result;
import com.medication.manage.ui.dashboard.MainActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 SharedPreferences
        preferences = getSharedPreferences("medication_prefs", MODE_PRIVATE);
        RetrofitClient.getInstance().setPreferences(preferences);

        // 检查是否已登录（有 Token）
        String token = preferences.getString("token", null);
        if (token != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // 登录按钮点击
        binding.btnLogin.setOnClickListener(v -> login());

        // 跳转注册
        binding.tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    /**
     * 执行登录
     */
    private void login() {
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入手机号和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("登录中...");

        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", password);

        RetrofitClient.getInstance().getApiService().login(params).enqueue(new Callback<Result<LoginResponse>>() {
            @Override
            public void onResponse(Call<Result<LoginResponse>> call, Response<Result<LoginResponse>> response) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("登录");

                if (response.body() != null && response.body().isSuccess()) {
                    LoginResponse data = response.body().getData();
                    // 保存 Token 和用户信息
                    preferences.edit()
                            .putString("token", data.getToken())
                            .putLong("userId", data.getUserId())
                            .putString("userName", data.getName())
                            .apply();

                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMsg() : "登录失败";
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<LoginResponse>> call, Throwable t) {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("登录");
                Toast.makeText(LoginActivity.this, "网络连接失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

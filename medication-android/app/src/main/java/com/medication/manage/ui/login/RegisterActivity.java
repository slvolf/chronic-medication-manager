package com.medication.manage.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.medication.manage.api.RetrofitClient;
import com.medication.manage.databinding.ActivityRegisterBinding;
import com.medication.manage.model.LoginResponse;
import com.medication.manage.model.Result;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 注册界面
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(v -> register());

        binding.tvGoLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String name = binding.etName.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6 || password.length() > 20) {
            Toast.makeText(this, "密码长度6-20位", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("注册中...");

        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        params.put("password", password);
        if (!name.isEmpty()) {
            params.put("name", name);
        }

        RetrofitClient.getInstance().getApiService().register(params).enqueue(new Callback<Result<LoginResponse>>() {
            @Override
            public void onResponse(Call<Result<LoginResponse>> call, Response<Result<LoginResponse>> response) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("注册");

                if (response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    finish(); // 返回登录页
                } else {
                    String msg = response.body() != null ? response.body().getMsg() : "注册失败";
                    Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result<LoginResponse>> call, Throwable t) {
                binding.btnRegister.setEnabled(true);
                binding.btnRegister.setText("注册");
                Toast.makeText(RegisterActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.medication.manage.api;

import android.content.SharedPreferences;

import com.medication.manage.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit 网络请求客户端
 * 单例模式，自动注入 Token
 */
public class RetrofitClient {

    // 从 BuildConfig 读取后端地址，在 app/build.gradle 的 buildConfigField 中配置
    // 模拟器默认 10.0.2.2:8080；真机请改为电脑局域网 IP
    private static final String BASE_URL = BuildConfig.BASE_URL.endsWith("/")
            ? BuildConfig.BASE_URL : BuildConfig.BASE_URL + "/";
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final ApiService apiService;
    private SharedPreferences preferences;

    private RetrofitClient() {
        // 日志拦截器
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Token 自动注入拦截器
        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                // 从 SharedPreferences 读取 Token
                if (preferences != null) {
                    String token = preferences.getString("token", null);
                    if (token != null) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                }

                builder.header("Content-Type", "application/json");
                Request request = builder.build();
                return chain.proceed(request);
            }
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    /**
     * 获取单例实例
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    /**
     * 设置 SharedPreferences（用于存储和读取 Token）
     */
    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    /**
     * 获取 API 服务接口
     */
    public ApiService getApiService() {
        return apiService;
    }
}

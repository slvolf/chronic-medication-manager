package com.medication.manage.api;

import com.medication.manage.model.ComplianceRate;
import com.medication.manage.model.LoginResponse;
import com.medication.manage.model.MedicationPlan;
import com.medication.manage.model.MedicationRecord;
import com.medication.manage.model.Result;
import com.medication.manage.model.UserInfo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 后端 API 接口定义
 */
public interface ApiService {

    // ========== 用户模块 ==========

    @POST("api/user/register")
    Call<Result<LoginResponse>> register(@Body Map<String, Object> body);

    @POST("api/user/login")
    Call<Result<LoginResponse>> login(@Body Map<String, Object> body);

    @GET("api/user/info")
    Call<Result<UserInfo>> getUserInfo();

    @PUT("api/user/info")
    Call<Result<UserInfo>> updateUserInfo(@Body Map<String, Object> body);

    // ========== 用药计划模块 ==========

    @POST("api/plan")
    Call<Result<MedicationPlan>> createPlan(@Body Map<String, Object> body);

    @PUT("api/plan")
    Call<Result<MedicationPlan>> updatePlan(@Body Map<String, Object> body);

    @DELETE("api/plan/{planId}")
    Call<Result<Void>> deletePlan(@Path("planId") Long planId);

    @GET("api/plan/list")
    Call<Result<List<MedicationPlan>>> getPlanList();

    @GET("api/plan/{planId}")
    Call<Result<MedicationPlan>> getPlanDetail(@Path("planId") Long planId);

    // ========== 打卡模块 ==========

    @POST("api/record/generate")
    Call<Result<Void>> generateTodayRecords();

    @POST("api/record/checkin")
    Call<Result<Void>> checkIn(@Body Map<String, Object> body);

    @PUT("api/record/missed/{recordId}")
    Call<Result<Void>> markMissed(@Path("recordId") Long recordId);

    @GET("api/record/today")
    Call<Result<List<MedicationRecord>>> getTodayRecords();

    @GET("api/record/date")
    Call<Result<List<MedicationRecord>>> getRecordsByDate(@Query("date") String date);

    @GET("api/record/range")
    Call<Result<List<MedicationRecord>>> getRecordsByRange(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate);

    // ========== 依从率统计 ==========

    @GET("api/compliance/today")
    Call<Result<ComplianceRate>> getTodayCompliance();

    @GET("api/compliance/weekly")
    Call<Result<ComplianceRate>> getWeeklyCompliance();
}

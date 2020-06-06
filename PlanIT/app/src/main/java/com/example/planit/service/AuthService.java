package com.example.planit.service;

import model.ChangeProfileDTO;
import model.LoginDTO;
import model.RegisterDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @POST("user/register")
    Call<ResponseBody> register(@Body RegisterDTO registerDTO);

    @POST("user/login")
    Call<ResponseBody> login(@Body LoginDTO loginDTO);

    @POST("user/googleLogin")
    Call<ResponseBody> googleLogin(@Body RegisterDTO googleLoginDTO);

    @PUT("user/changeUser")
    Call<ResponseBody> changeUser(@Body ChangeProfileDTO changeProfileDTO);

}

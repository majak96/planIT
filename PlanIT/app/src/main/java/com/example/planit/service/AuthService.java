package com.example.planit.service;

import model.ChangeProfileDTO;
import model.LoginDTO;
import model.UserInfoDTO;
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
    Call<ResponseBody> register(@Body UserInfoDTO userInfoDTO);

    @POST("user/login")
    Call<UserInfoDTO> login(@Body LoginDTO loginDTO);

    @POST("user/googleLogin")
    Call<ResponseBody> googleLogin(@Body UserInfoDTO googleLoginDTO);

    @PUT("user/changeUser")
    Call<ResponseBody> changeUser(@Body ChangeProfileDTO changeProfileDTO);

}

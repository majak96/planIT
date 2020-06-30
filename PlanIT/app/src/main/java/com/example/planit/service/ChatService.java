package com.example.planit.service;

import model.MessageDTO;
import model.RegisterDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @POST("chat/message")
    Call<ResponseBody> sendMessage(@Body MessageDTO messageDTO);

    @GET("chat/allMessages")
    Call<ResponseBody> getMessagse(@Query("teamId") Integer teamId);

}

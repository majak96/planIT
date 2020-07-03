package com.example.planit.service;

import java.util.List;

import model.MessageDTO;
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

    @GET("chat/all")
    Call<List<MessageDTO>> getMessagse(@Query("teamId") Long teamId);

}

package com.example.planit.service;

import model.TaskSyncDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface TaskService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @GET("task/sync")
    Call<TaskSyncDTO> synchronizationTask(@Query("email") String email, @Query("date") Long date);
}

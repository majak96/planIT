package com.example.planit.service;

import model.TaskSyncDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import model.CreateTaskResponseDTO;
import model.TaskDTO;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TaskService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @GET("task/sync")
    Call<TaskSyncDTO> synchronizationTask(@Query("email") String email, @Query("date") Long date);

    @POST("task")
    Call<CreateTaskResponseDTO> createTask(@Body TaskDTO taskDTO);

    @PUT("task/{taskId}")
    Call<CreateTaskResponseDTO> updateTask(@Path("taskId") Integer taskId, @Body TaskDTO taskDTO, @Query("date") Long date);

    @DELETE("task/{taskId}")
    Call<ResponseBody> deleteTask(@Path("taskId") Integer taskId);
}

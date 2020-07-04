package com.example.planit.service;

import model.HabitSyncDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface HabitService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })


    @GET("habit/sync")
    Call<HabitSyncDTO> synchronizationHabits(@Query("email") String email, @Query("date") Long date);

}

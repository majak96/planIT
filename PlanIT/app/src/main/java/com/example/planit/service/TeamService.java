package com.example.planit.service;

import model.ChangeProfileDTO;
import model.CreateTeamDTO;
import model.LoginDTO;
import model.RegisterDTO;
import model.TeamMemberDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface TeamService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @POST("team/create")
    Call<ResponseBody> createTeam(@Body CreateTeamDTO createTeamDTO);

    @POST("team/addMember")
    Call<ResponseBody> addMember(@Body TeamMemberDTO teamMemberDTO);

}

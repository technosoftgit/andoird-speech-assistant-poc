package com.technosoft.rylee.network;

import com.technosoft.rylee.model.InputRequest;
import com.technosoft.rylee.model.InputResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("/detect")
    Call<InputResponse> postInput(@Body InputRequest inputRequest);
}

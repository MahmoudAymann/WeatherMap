package com.maymanm.weathermap.repository;

import com.maymanm.weathermap.models.directions.DirectionsModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by MahmoudAyman on 6/11/2019.
 **/
public interface DirectionServiceApi {

    @GET("json")
    Call<DirectionsModel> getDirections(@Query("origin")String origin,
                                        @Query("destination")String destination,
                                        @Query("key")String key);


}

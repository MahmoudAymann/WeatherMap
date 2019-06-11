package com.maymanm.weathermap.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.maymanm.weathermap.Client;
import com.maymanm.weathermap.Constants;
import com.maymanm.weathermap.models.directions.DirectionsModel;
import com.maymanm.weathermap.models.directions.Step;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by MahmoudAyman on 6/11/2019.
 **/
public class DirectionRepository {
    private static DirectionRepository directionRepository;
    private DirectionServiceApi directionServiceApi;


    private DirectionRepository() {
        Retrofit retrofit = Client.getInstance(Constants.BASE_DIRECTIONS_URL);
        directionServiceApi = retrofit.create(DirectionServiceApi.class);
    }


    public synchronized static DirectionRepository getInstance() {
        if (directionRepository == null) {
            directionRepository = new DirectionRepository();
        }
        return directionRepository;
    }


    public LiveData<DirectionsModel> getDirection(String origin, String dest, String key) {
        MutableLiveData<DirectionsModel> data = new MutableLiveData<>();
        directionsCall(origin, dest, key).enqueue(new Callback<DirectionsModel>() {
            @Override
            public void onResponse(@NonNull Call<DirectionsModel> call, @NonNull Response<DirectionsModel> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DirectionsModel> call, @NonNull Throwable t) {
                data.setValue(null);
            }
        });
        return data;
    }

    public LiveData<String[]> getPolyLine(DirectionsModel directionsModel) {
        MutableLiveData<String[]> polyline = new MutableLiveData<>();
        String[] polylineArray = null;

        if (directionsModel != null && directionsModel.getStatus().equals("OK")) {
            int count = directionsModel.getRoutes().get(0).getLegs().get(0).getSteps().size();
            Step[] steps = directionsModel.getRoutes().get(0).getLegs().get(0).getSteps().toArray(new Step[count]);
            polylineArray = new String[count];

            for (int i = 0; i < count; i++) {
                polylineArray[i] = steps[i].getPolyline().getPoints();
            }
        }

        polyline.setValue(polylineArray);
        return polyline;
    }


    private Call<DirectionsModel> directionsCall(String origin, String destination, String key) {
        return directionServiceApi.getDirections(origin, destination, key);
    }

}
